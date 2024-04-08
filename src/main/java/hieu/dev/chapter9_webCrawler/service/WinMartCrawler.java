package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class WinMartCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        HttpEntity<String> requestEntity = new HttpEntity<>("", BaseHttpClient.headers);
        ResponseEntity<String> provinceResponse = BaseHttpClient.restTemplate.exchange("https://api-crownx.winmart.vn/mt/api/web/v1/provinces/all-winmart", HttpMethod.GET, requestEntity, String.class);
        gson.fromJson(org.unbescape.html.HtmlEscape.unescapeHtml(provinceResponse.getBody()), JsonObject.class).getAsJsonArray("data")
                .asList().stream().map(JsonElement::getAsJsonObject).map(provinceElement -> provinceElement.get("code").getAsString())
                .forEach(provinceCode -> {
                    String url = UriComponentsBuilder.fromHttpUrl("https://api-crownx.winmart.vn/mt/api/web/v1/store-by-province")
                            .queryParam("PageNumber", 1)
                            .queryParam("PageSize", 1000)
                            .queryParam("ProvinceCode", provinceCode).toUriString();
                    ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
                    gson.fromJson(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody()), JsonObject.class)
                            .getAsJsonArray("data")
                            .asList().stream().map(JsonElement::getAsJsonObject)
                            .flatMap(districtElement -> districtElement.getAsJsonArray("wardStores").asList().stream())
                            .map(JsonElement::getAsJsonObject)
                            .flatMap(wardElement -> wardElement.getAsJsonArray("stores").asList().stream())
                            .map(JsonElement::getAsJsonObject)
                            .forEach(store -> {
                                JsonElement latitudeElement = store.get("latitude");
                                if(Objects.isNull(latitudeElement) || latitudeElement.isJsonNull() || latitudeElement.getAsDouble() == 0) return;
                                String name = store.get("storeName").getAsString();
                                String address = store.get("officeAddress").getAsString();
                                Double lat = store.get("latitude").getAsDouble();
                                Double lon = store.get("longitude").getAsDouble();
                                BaseEntity entity = BaseEntity.builder()
                                        .address(address).name(name).lat(lat).lon(lon).build();
                                System.out.println(entity);
                                mongoTemplate.save(entity, "win_mart_places");
                            });
                    Utils.sleep(500);
                });
    }

}
