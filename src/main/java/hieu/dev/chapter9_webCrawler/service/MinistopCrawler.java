package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class MinistopCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        String url = "https://www.ministop.vn/get-shop";
        ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
        gson.fromJson(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody()), JsonArray.class).forEach(jsonElement -> {
            JsonObject storeObj = jsonElement.getAsJsonObject();
            String nameVi = storeObj.get("name_vi").getAsString();
            String addressVi = storeObj.get("address_vi").getAsString();
            double lat = storeObj.get("geo_lat").getAsDouble();
            double lon = storeObj.get("geo_long").getAsDouble();
            BaseEntity baseEntity = BaseEntity.builder()
                    .lon(lon).lat(lat).address(addressVi).name(nameVi).build();
            System.out.println(baseEntity);
            mongoTemplate.save(baseEntity, "ministop_places");
        });
    }
}
