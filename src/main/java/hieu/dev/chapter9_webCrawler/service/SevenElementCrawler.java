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

@Service
public class SevenElementCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String url = "https://loyalty.sevensystem.vn/api/public/stores?page=0&page_size=20000";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        gson.fromJson(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody()), JsonObject.class).getAsJsonArray("content")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String address = store.get("address").getAsString();
                    double lat = store.get("lat").getAsDouble();
                    double lon = store.get("lng").getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .address(address).lat(lat).lon(lon).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "seven_element_places");
                });
    }
}
