package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class VntripcomCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://vn.trip.com/hotels").get();
        String data = document.html().split("window.IBU_HOTEL=")[1].split("};")[0] + "}";
        Stream<JsonObject> regions = gson.fromJson(data, JsonObject.class).getAsJsonObject("initData").getAsJsonObject("htlsData")
                .getAsJsonArray("inboundCities").asList().stream().map(JsonElement::getAsJsonObject);
        regions.map(regionObj -> {
            String id = regionObj.get("id").getAsString();
            String name = regionObj.get("name").getAsString();
            return new VntripcomRegion(id, name);
        }).forEach(region -> {
            int startPage = tracePage0(region.getName(), "vntripcom_places");
            for (int page = startPage; ; page++) {
                log.info("Handling page - region: {} - {}", page, gson.toJson(region));

                MediaType mediaType = MediaType.parse("application/json");
                String requestBodyStr = String.format("{\"guideLogin\":\"T\",\"search\":{\"sessionId\":\"%s\",\"preHotelCount\":0,\"preHotelIds\":[],\"checkIn\":\"20240326\",\"checkOut\":\"20240327\",\"sourceFromTag\":\"\",\"filters\":[{\"filterId\":\"17|1\",\"value\":\"1\",\"type\":\"17\",\"subType\":\"2\",\"sceneType\":\"17\"},{\"filterId\":\"80|0|1\",\"value\":\"0\",\"type\":\"80\",\"subType\":\"2\",\"sceneType\":\"80\"},{\"filterId\":\"29|1\",\"value\":\"1|2\",\"type\":\"29\"}],\"pageCode\":10320668148,\"location\":{\"geo\":{\"countryID\":111,\"provinceID\":0,\"cityID\":%s,\"districtID\":0,\"oversea\":true},\"coordinates\":[]},\"pageIndex\":%d,\"pageSize\":10,\"needTagMerge\":\"T\",\"roomQuantity\":1,\"orderFieldSelectedByUser\":false,\"hotelId\":0,\"tripWalkDriveSwitch\":\"T\",\"resultType\":\"CT\",\"nearbyHotHotel\":{},\"recommendTimes\":0,\"crossPromotionId\":\"\",\"travellingForWork\":false},\"batchRefresh\":{\"batchId\":\"18e7a1696421hpt1d5i2\",\"batchSeqNo\":1},\"queryTag\":\"NORMAL\",\"mapType\":\"GOOGLE\",\"extends\":{\"crossPriceConsistencyLog\":\"\",\"NewTaxDescForAmountshowtype0\":\"B\",\"MealTagDependOnMealType\":\"T\",\"MultiMainHotelPics\":\"T\",\"enableDynamicRefresh\":\"F\",\"isFirstDynamicRefresh\":\"T\",\"ExposeBedInfos\":false},\"head\":{\"platform\":\"PC\",\"clientId\":\"1711426060449.145bLrAWF4Nm\",\"bu\":\"ibu\",\"group\":\"TRIP\",\"aid\":\"1078328\",\"sid\":\"2036522\",\"ouid\":\"ctag.hash.e8b6908620bd\",\"caid\":\"1078328\",\"csid\":\"2036522\",\"couid\":\"ctag.hash.e8b6908620bd\",\"region\":\"VN\",\"locale\":\"vi-VN\",\"timeZone\":\"7\",\"currency\":\"VND\",\"p\":\"92020585947\",\"pageID\":\"10320668148\",\"deviceID\":\"PC\",\"clientVersion\":\"0\",\"frontend\":{\"vid\":\"1711426060449.145bLrAWF4Nm\",\"sessionID\":\"2\",\"pvid\":\"14\"}}}",
                        UUID.randomUUID(), region.getId(), page);
                RequestBody body = RequestBody.create(mediaType, requestBodyStr);
                Request request = new Request.Builder()
                        .url("https://vn.trip.com/htls/getHotelList")
                        .method("POST", body)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .addHeader("content-type", "application/json")
                        .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .build();
                try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    JsonArray places = gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("hotelList");
                    if (places.isEmpty()) return;
                    int finalPage = page;
                    places.asList().stream().map(JsonElement::getAsJsonObject)
                            .forEach(place -> {
                                try {
                                    JsonObject hotelBasicInfo = place.getAsJsonObject("hotelBasicInfo");
                                    String id = hotelBasicInfo.get("hotelId").getAsString();
                                    String name = hotelBasicInfo.get("hotelName").getAsString();
                                    String address = hotelBasicInfo.get("hotelAddress").getAsString();

                                    JsonObject positionInfo = place.getAsJsonObject("positionInfo");
                                    double lat = positionInfo.getAsJsonObject("coordinate").get("lat").getAsDouble();
                                    double lon = positionInfo.getAsJsonObject("coordinate").get("lng").getAsDouble();
                                    BaseEntity baseEntity = BaseEntity.builder()
                                            .id(id).name(name).address(address).lon(lon).lat(lat).placeCode(region.getName()).page(finalPage).build();
                                    log.info("Place: {}", gson.toJson(baseEntity));
                                    mongoTemplate.save(baseEntity, "vntripcom_places");
                                } catch (Exception e) {
                                    log.error("Error while handle place: {}", e.getMessage(), e);
                                }

                            });
                } catch (Exception e) {
                    log.error("Error while handle place: {}", e.getMessage(), e);
                } finally {
                    Utils.sleepRandom(2000);
                }
            }
        });
    }

    @Data
    @AllArgsConstructor
    public static class VntripcomRegion {
        String id;
        String name;
    }
}
