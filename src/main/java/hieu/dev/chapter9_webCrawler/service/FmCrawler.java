package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class FmCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        Request request = new Request.Builder()
                .url("https://api.fmplus.com.vn/api/1.0/master/branch?type=0")
                .get()
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("x-apikey", "X2geZ7rDEDI73K1vqwEGStqGtR90JNJ0K4sQHIrbUI3YISlv")
                .addHeader("x-requestid", "4fb31b8a-394e-4d05-9381-f6f17deb69e0")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String body = response.body().string();
            gson.fromJson(body, JsonArray.class)
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String name = store.get("Name").getAsString();
                        String address = store.get("Address").getAsString() + ", " +
                                store.get("CommuneName").getAsString() + ", " +
                                store.get("DistrictName").getAsString() + ", " +
                                store.get("ProvinceName").getAsString();
                        if (Objects.isNull(store.get("LatOfMap")) || store.get("LatOfMap").isJsonNull()) return;
                        double lat = store.get("LatOfMap").getAsDouble();
                        double lon = store.get("LongOfMap").getAsDouble();

                        BaseEntity entity = BaseEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "fm_places");
                    });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
