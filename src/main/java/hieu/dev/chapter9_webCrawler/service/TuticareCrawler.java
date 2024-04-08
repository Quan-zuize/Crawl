package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class TuticareCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.tuticare.com/ajax/get_json.php")
                .queryParam("action", "store")
                .queryParam("province", "0").toUriString();
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
        System.out.println(body);
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        List<JsonElement> items = jsonObject.getAsJsonArray("list").asList();
        items.stream().map(item -> {
            JsonObject locationObject = item.getAsJsonObject();
            System.out.println(locationObject);
            BaseEntity baseEntity = new BaseEntity();
            baseEntity.setId(locationObject.get("id").getAsString());
            baseEntity.setName(locationObject.get("store_name").getAsString());
            String address = locationObject.get("address").getAsString();
            if (address.split("Tel:").length > 1) {
                address = address.split("Tel:")[0].trim();
            }
            baseEntity.setAddress(address);
            try {
                baseEntity.setLat(locationObject.get("latitude").getAsDouble());
                baseEntity.setLon(locationObject.get("longtitude").getAsDouble());
            } catch (Exception e) {
                log.warn("Skipping");
                return null;
            }
            return baseEntity;
        }).filter(Objects::nonNull).forEach(entity -> mongoTemplate.save(entity, "tuticare_places"));
    }
}
