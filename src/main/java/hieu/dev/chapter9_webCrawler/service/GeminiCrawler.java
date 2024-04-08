package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawlGemini() throws IOException {
        String url = "https://geminicoffee.vn/wp-admin/admin-ajax.php?action=dvls_loadlastest_store";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);
        String body = responseEntity.getBody();
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        JsonArray storeElements = jsonObject.getAsJsonArray("data");
        for (JsonElement storeElement : storeElements) {
            JsonObject storeObject = storeElement.getAsJsonObject();
            String id = storeObject.get("id").getAsString();
            String name = storeObject.get("name").getAsString();
            String address = storeObject.get("address").getAsString();
            Double lat = storeObject.get("maps_lat").getAsDouble();
            Double lon = storeObject.get("maps_lng").getAsDouble();
            BaseEntity entity = BaseEntity.builder()
                    .id(id).name(name).address(address).lat(lat).lon(lon).build();
            mongoTemplate.save(entity, "gemini_places");
        }
    }
}
