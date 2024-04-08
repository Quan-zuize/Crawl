package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class Shine30Crawler extends BaseSeleniumCrawler {
    public void crawl() {
        Request request = new Request.Builder()
                .url("https://storage.30shine.com/web/v3/configs/get_all_salon.json")
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .build();
        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute();){
            String body = response.body().string();
            gson.fromJson(body, JsonObject.class)
                    .getAsJsonArray("data").asList()
                    .stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String name = store.get("name").getAsString();
                        String address = store.get("addressNew").getAsString();
                        String embedMap = store.get("embedMap").getAsString();
                        saveDataWithHref(address, name, embedMap, "shine_30_places");
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }
}
