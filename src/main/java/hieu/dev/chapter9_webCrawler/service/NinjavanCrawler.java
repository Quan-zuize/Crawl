package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class NinjavanCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        Request request = new Request.Builder()
                .url("https://walrus.ninjavan.co/vn/dp/2.0/dps?allow_shipper_send=true")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .build();
        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            gson.fromJson(responseBody, JsonObject.class).getAsJsonArray("dps")
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String name = store.get("name").getAsString();
                        String address = store.get("address_1").getAsString();
                        double lat = store.get("latitude").getAsDouble();
                        double lon = store.get("longitude").getAsDouble();
                        BaseEntity entity = BaseEntity.builder().name(name).address(address).lon(lon).lat(lat).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "ninjavan_places");
                    });
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
        }
    }
}
