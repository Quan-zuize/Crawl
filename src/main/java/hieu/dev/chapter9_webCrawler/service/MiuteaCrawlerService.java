package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.Utils.gson;
import static hieu.dev.chapter9_webCrawler.Utils.normalize;

@Service
@Slf4j
public class MiuteaCrawlerService{
    List<BaseEntity> stores = new LinkedList<>();

    List<String> cities = getListCity();

    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");

        cities.forEach(city -> {
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("action", "dvls_load_localstores")
                    .addFormDataPart("cityid", city)
                    .addFormDataPart("districtid", "null")
                    .addFormDataPart("nonce", "50489b7689")
                    .build();
            Request request = new Request.Builder()
                    .url("https://miutea.vn/wp-admin/admin-ajax.php")
                    .method("POST", body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                gson.fromJson(responseBody, JsonObject.class).get("data").getAsJsonArray()
                        .asList().stream().map(JsonElement::getAsJsonObject)
                        .forEach(e -> {
                            BaseEntity baseEntity = new BaseEntity();
                            baseEntity.setName(normalize(e.get("name").getAsString()));
                            baseEntity.setAddress(normalize(e.get("address").getAsString()));
                            baseEntity.setLat(e.get("maps_lat").getAsDouble());
                            baseEntity.setLon(e.get("maps_lng").getAsDouble());
                            stores.add(baseEntity);
                        });
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage(), e);
            }
        });

        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BaseEntity.class, "Miutea");
        stores.forEach(bulkOperations::insert);
        bulkOperations.execute();
        log.info("Insert successfully");
    }

    private List<String> getListCity() {
        try {
            String data = Files.readString(Paths.get("src/main/java/hieu/dev/chapter9_webCrawler/service/MiuteaCitiesData.txt"));
            data = data.split(",\"maps_zoom\"")[0];

            return gson.fromJson(data, JsonObject.class).get("local_address").getAsJsonArray()
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .filter(asJsonObject -> asJsonObject.has("district"))
                    .map(asJsonObject -> normalize(asJsonObject.get("id").getAsString()))
                    .toList();
        } catch (IOException e) {
            log.error("Error while get page: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }
}
