package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class GongChaCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawlGongCha() {
        String url = "https://gongcha.com.vn/wp-admin/admin-ajax.php?action=load_list_location&cid=-1";
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String body = responseEntity.getBody();
        JsonArray cityElements = gson.fromJson(body, JsonArray.class);

        for (JsonElement cityElement : cityElements) {
            JsonArray locationElements = cityElement.getAsJsonObject().getAsJsonArray("location");
            if(Objects.isNull(locationElements) || locationElements.isJsonNull()) continue;
            for (JsonElement locationElement : locationElements) {
                BaseEntity baseEntity = BaseEntity.fromGongCha(locationElement);
                mongoTemplate.save(baseEntity, "gong_cha_places");
                log.info("Location: {}", gson.toJson(baseEntity));
            }
        }
    }

}
