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
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class IvivuCrawler extends BaseSeleniumCrawler {

    public void crawl() throws IOException {
        List<IvivuRegion> regions = getRegions();
        regions.forEach(region -> {
            String regionId = region.getId();
            int startPage = tracePage(regionId, "ivivu_places");
            for (int page = startPage; ; page++) {
                String pageUrl = UriComponentsBuilder.fromHttpUrl("https://www.ivivu.com/hotelslist")
                        .queryParam("regionId", regionId)
                        .queryParam("page", page)
                        .queryParam("pageSize", 100).toUriString();
                log.info("page url: {}", pageUrl);
                Request request = new Request.Builder()
                        .url(pageUrl)
                        .get()
                        .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .build();
                try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                    String body = response.body().string();
                    JsonArray stores = gson.fromJson(body, JsonObject.class).getAsJsonArray("List");
                    if (stores.isEmpty()) return;
                    int finalPage = page;
                    stores.asList().stream().map(JsonElement::getAsJsonObject).forEach(store -> {
                        try {
                            String id = store.get("HotelId").getAsString();
                            String name = store.get("HotelName").getAsString();
                            String address = store.get("Address").getAsString();
                            double lat = store.get("Lat").getAsDouble();
                            double lon = store.get("Lon").getAsDouble();
                            String placeCode = region.getName();
                            BaseEntity entity = BaseEntity.builder()
                                    .id(id).address(address).name(name).lon(lon).lat(lat).page(finalPage).placeCode(placeCode).build();
                            log.info("Place: {}", entity);
                            mongoTemplate.save(entity, "ivivu_places");
                        } catch (Exception e) {
                            log.error("Error while handle store: {}", store);
                        }
                    });
                } catch (Exception e) {
                    log.error("Error while handle request: {}", e.getMessage(), e);
                } finally {
                    Utils.sleepRandom(1000);
                }
            }

        });
    }

    private List<IvivuRegion> getRegions() throws IOException {
        if (mongoTemplate.exists(new Query(), "ivivu_regions")) {
            return mongoTemplate.findAll(IvivuRegion.class, "ivivu_regions");
        }

        String url = "https://www.ivivu.com/khach-san-ha-noi";
        Document document = Jsoup.connect(url).get();
        List<String> regionUrls = new java.util.ArrayList<>(document.select("#collapseThree a[href]")
                .stream().map(region -> region.absUrl("href"))
                .toList());
        regionUrls.add(url);

        List<IvivuRegion> ivivuRegions = new ArrayList<>();
        for (String regionUrl : regionUrls) {
            try {
                List<String> list = new ArrayList<>(List.of(regionUrl.split("-")));
                list.remove(0);
                list.remove(0);
                String name = String.join(" ", list);
                IvivuRegion ivivuPlace = new IvivuRegion();
                ivivuPlace.setName(name);
                ivivuPlace.setId(Jsoup.connect(regionUrl).get().getElementById("regionid").val());
                ivivuPlace.setUrl(regionUrl);
                ivivuRegions.add(ivivuPlace);
            } catch (Exception e) {
                log.error("Error while get category id: {}", regionUrl);
            } finally {
                Utils.sleepRandom(50);
            }
        }
        ivivuRegions.forEach(entity -> mongoTemplate.save(entity, "ivivu_regions"));
        return ivivuRegions;
    }

    @Data
    public static class IvivuRegion {
        String id;
        String url;
        String name;
    }
}
