package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Service
@Slf4j
public class AeonMaxValuCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        String url = "https://aeonmaxvalu.com.vn/cua_hang/GetDatas?pageSize=1000&pageNumber=1";
        HttpHeaders httpHeaders = new HttpHeaders(BaseHttpClient.headers);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                .headers(Headers.of(httpHeaders.toSingleValueMap()))
                .build();
        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
            String body = response.body().string();
            gson.fromJson(gson.fromJson(body, String.class), JsonObject.class).getAsJsonArray("Items")
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String name = store.get("Name").getAsString();
                        String address = store.get("Address").getAsString();
                        if (store.get("Latitude").isJsonNull()) {
                            String googleAddress = store.get("GoogleAddress").getAsString();
                            saveDataWithHref(address, name, googleAddress, "aeon_max_valu_places");
                            return;
                        }
                        double lat = store.get("Latitude").getAsDouble();
                        double lon = store.get("Longitude").getAsDouble();
                        BaseEntity baseEntity = BaseEntity.builder()
                                .lat(lat).lon(lon).address(address).name(name).build();
                        System.out.println(baseEntity);
                        mongoTemplate.save(baseEntity, "aeon_max_valu_places");
                    });
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
        }
    }
}
