package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class CanifaCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() throws IOException {
        List<String> provinceIds = getProvinceIds();
        provinceIds.forEach(provinceId -> {
            String url = UriComponentsBuilder.fromHttpUrl("https://canifa.com/api/ext/store-locator/find")
                    .queryParam("city_id", provinceId).toUriString();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
            String body = response.getBody();
            gson.fromJson(body, JsonObject.class).getAsJsonArray("result")
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String address = store.get("address").getAsString();
                        String name = store.get("store_name").getAsString();
                        if (Objects.isNull(store.get("latitude")) || store.get("latitude").isJsonNull()) return;
                        double lat = store.get("latitude").getAsDouble();
                        double lon = store.get("longitude").getAsDouble();
                        BaseEntity entity = BaseEntity.builder()
                                .address(address).name(name).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "canifa_crawler");
                    });
        });


    }

    public static List<String> getProvinceIds() {
        ResponseEntity<String> response = restTemplate.exchange("https://canifa.com/api/ext/store-locator/city?country_id=VN", HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = response.getBody();
        List<String> result = gson.fromJson(body, JsonObject.class).getAsJsonArray("result")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .map(province -> province.get("text").getAsString()).toList();
        System.out.println(result);
        return result;
    }
}
