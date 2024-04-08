package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class MytourCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {

        List<MytourCrawler.MytourRegion> regions = getRegions();
        regions.forEach(region -> {
            String regionId = region.getId();
            int startPage = tracePage(region.getName(), "mytour_places");
            for (int page = 1; ; page++) {
                log.info("Hanle page - region: {} - {}", page, gson.toJson(region));
                MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
                RequestBody requestBody = RequestBody.create(mediaType, String.format("{\"page\":%d,\"filters\":{\"amenities\":[],\"services\":[],\"stars\":[],\"ratingRanges\":[],\"types\":[],\"chainIds\":[],\"tags\":[],\"categoryTypes\":[\"hotel\",\"special_place\"]},\"aliasCode\":\"%s\",\"listing\":\"%s\",\"size\":100,\"checkIn\":\"25-03-2024\",\"checkOut\":\"26-03-2024\",\"adults\":2,\"rooms\":1,\"children\":0,\"sortBy\":\"\",\"isFirstRequest\":false,\"locationCode\":null,\"searchType\":\"location\",\"travellerType\":1,\"expandEntirePlace\":false,\"useBasePrice\":true}",
                        page, regionId, region.getUrl().split("/")[6]));
                Request request = new Request.Builder()
                        .url("https://apis.tripi.vn/hotels/v3/hotels/availability")
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .method("POST", requestBody)
                        .addHeader("content-type", "application/json;charset=UTF-8")
                        .addHeader("deviceid", "1709014458870-0.7156824412853477")
                        .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .build();
                try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                    String body = response.body().string();
                    JsonArray stores = gson.fromJson(body, JsonObject.class).getAsJsonObject("data").getAsJsonArray("items");
                    if (stores.isEmpty()) {
                        log.info("data is empty. skipping...");
                        return;
                    }
                    int finalPage = page;
                    stores.asList().stream().map(JsonElement::getAsJsonObject).forEach(store -> {
                        try {
                            String id = store.get("id").getAsString();
                            String name = store.get("name").getAsString();
                            JsonObject addressObj = store.getAsJsonObject("address");
                            String address = addressObj.get("address").getAsString();
                            double lat = addressObj.getAsJsonObject("coordinate").get("latitude").getAsDouble();
                            double lon = addressObj.getAsJsonObject("coordinate").get("longitude").getAsDouble();
                            String placeCode = region.getName();
                            BaseEntity entity = BaseEntity.builder()
                                    .id(id).address(address).name(name).lon(lon).lat(lat).page(finalPage).placeCode(placeCode).build();
                            log.info("Place: {}", entity);
                            mongoTemplate.save(entity, "mytour_places");
                        } catch (Exception e) {
                            log.error("Error while handle store: {}", store);
                        }
                    });
                } catch (Exception e) {
                    log.error("Error while handle request: {}", e.getMessage(), e);
                } finally {
                    Utils.sleepRandom(3000);
                }
            }

        });
    }

    private static List<MytourCrawler.MytourRegion> getRegions() throws IOException {
        String url = "https://mytour.vn/khach-san";
        Document document = Jsoup.connect(url).get();
        List<String> regionUrls = new java.util.ArrayList<>(document.select("a.MuiTypography-root")
                .stream().map(region -> region.absUrl("href"))
                .filter(regionUrl -> regionUrl.contains("/khach-san/"))
                .toList());

        List<MytourCrawler.MytourRegion> mytourRegions = new ArrayList<>();
        for (String regionUrl : regionUrls) {
            try {
                List<String> paths = List.of(regionUrl.split("/"));
                List<String> list = new ArrayList<>(List.of(paths.get(6).split("-")));
                list.remove(0);
                list.remove(0);
                list.remove(0);
                String name = String.join(" ", list).replace(".html", "");
                MytourCrawler.MytourRegion mytourPlace = new MytourCrawler.MytourRegion();
                mytourPlace.setName(name);
                mytourPlace.setId(paths.get(5));
                mytourPlace.setUrl(regionUrl);
                mytourRegions.add(mytourPlace);
            } catch (Exception e) {
                log.error("Error while get category id: {}", regionUrl);
            } finally {
                Utils.sleepRandom(50);
            }
        }
        return mytourRegions;
    }

    @Data
    public static class MytourRegion {
        String id;
        String url;
        String name;
    }
}
