package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Service
@Slf4j
public class KatinatCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawlKatinat() throws IOException {
        String url = "https://katinat.vn/wp-admin/admin-ajax.php?action=dvls_loadlastest_store";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);
        String body = responseEntity.getBody();
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        JsonArray storeElements = jsonObject.getAsJsonArray("data");
        for (JsonElement storeElement : storeElements) {
            JsonObject storeObject = storeElement.getAsJsonObject();
            String id = storeObject.get("id").getAsString();
            String name = storeObject.get("name").getAsString();
            String address = storeObject.get("address").getAsString();
            String[] coordinates = storeObject.get("maps_lat").getAsString().split(",");

            double lat, lon;
            if(coordinates.length == 2) {
                lat = Double.parseDouble(coordinates[0]);
                lon = Double.parseDouble(coordinates[1]);
            } else {
                lat = storeObject.get("maps_lat").getAsDouble();
                lon = storeObject.get("maps_lng").getAsDouble();
            }

            BaseEntity entity = BaseEntity.builder()
                    .id(id).name(name).address(address).lat(lat).lon(lon).build();
            mongoTemplate.save(entity, "katinat_places");
        }
    }
}
