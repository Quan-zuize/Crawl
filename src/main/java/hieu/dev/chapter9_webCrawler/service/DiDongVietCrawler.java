package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static hieu.dev.chapter9_webCrawler.client.BaseHttpClient.*;

@Service
public class DiDongVietCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ResponseEntity<String> response = restTemplate.exchange("https://ecomws.didongviet.vn/fe/v1/stores?limit=999", HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        List<JsonElement> stores = jsonObject.getAsJsonObject("data").getAsJsonArray("stores").asList();
        stores.stream().map(JsonElement::getAsJsonObject).forEach(store -> {
            String address = store.get("store_name").getAsString();
            double lat = store.get("latitude").getAsDouble();
            double lon = store.get("longitude").getAsDouble();
            BaseEntity baseEntity = BaseEntity.builder()
                    .address(address).lat(lat).lon(lon).build();
            mongoTemplate.save(baseEntity, "didongviet_places");
            System.out.println(baseEntity);
        });
    }

}
