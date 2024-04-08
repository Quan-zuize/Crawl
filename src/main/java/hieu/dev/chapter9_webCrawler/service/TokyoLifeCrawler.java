package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class TokyoLifeCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        Request request = new Request.Builder()
                .url("https://api-prod.tokyolife.vn/cms-api/api/v1/stores?page=1&limit=1000&is_active=true&state=2&flag=1")
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("Accept", "application/json, text/plain, */*")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String body = response.body().string();
            gson.fromJson(body, JsonObject.class).getAsJsonObject("data")
                    .getAsJsonArray("data").asList().stream().map(JsonElement::getAsJsonObject)
                    .filter(store -> Objects.nonNull(store.getAsJsonObject("location").get("latitude")) && Strings.isNotEmpty(store.getAsJsonObject("location").get("latitude").getAsString()))
                    .forEach(store -> {
                        String name = store.get("name").getAsString();
                        String address = store.getAsJsonObject("address").get("address").getAsString();
                        double lat = store.getAsJsonObject("location").get("latitude").getAsDouble();
                        double lon = store.getAsJsonObject("location").get("longitude").getAsDouble();
                        BaseEntity entity = BaseEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "tokyo_life_places");
                    });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
