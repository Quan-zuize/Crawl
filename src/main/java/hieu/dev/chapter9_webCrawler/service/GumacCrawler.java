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

import java.util.Objects;

@Service
public class GumacCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String url = "https://cms.gumac.vn/api/v1/branchs?order_by=display_order&order_type=asc";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = response.getBody();
        gson.fromJson(body, JsonObject.class).getAsJsonArray("data")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String address = store.get("address").getAsString();
                    String name = store.get("title").getAsString();
                    if(Objects.isNull(store.get("lat")) || store.get("lat").isJsonNull()) return;
                    double lat = store.get("lat").getAsDouble();
                    double lon = store.get("lng").getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .address(address).name(name).lat(lat).lon(lon).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "guma_crawler");
                });
    }
}
