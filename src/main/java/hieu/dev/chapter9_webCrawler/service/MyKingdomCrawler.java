package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class MyKingdomCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ResponseEntity<String> response = restTemplate.exchange("https://stockist.co/api/v1/u18077/locations/all", HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
        JsonArray elements = gson.fromJson(body, JsonArray.class);
        elements.forEach(element -> {
            JsonObject locationObject = element.getAsJsonObject();
            BaseEntity baseEntity = new BaseEntity();
            baseEntity.setId(locationObject.get("id").getAsString());
            if(Strings.isNotEmpty(locationObject.get("name").getAsString())) {
                baseEntity.setName(locationObject.get("name").getAsString());
            }
            baseEntity.setAddress(locationObject.get("address_line_1").getAsString());
            baseEntity.setLat(locationObject.get("latitude").getAsDouble());
            baseEntity.setLon(locationObject.get("longitude").getAsDouble());
            mongoTemplate.save(baseEntity, "my_kingdom_places");
        });
    }
}
