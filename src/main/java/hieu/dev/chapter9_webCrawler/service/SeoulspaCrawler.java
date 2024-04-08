package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class SeoulspaCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("action","map_embed_by_type_stores")
                .build();
        Request request = new Request.Builder()
                .url("https://seoulspa.vn/wp-admin/admin-ajax.php")
                .method("POST", body)
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("Cookie", "PHPSESSID=jqedn4vdet5fp9fvgp971n9dfo")
                .build();
        try(Response response = BaseHttpClient.okHttpClient.newCall(request).execute()){
            String string = response.body().string();
            gson.fromJson(string, JsonObject.class)
                    .getAsJsonArray("data").asList().stream().map(JsonElement::getAsJsonObject)
                    .flatMap(storeType -> storeType.getAsJsonArray("store").asList().stream().map(JsonElement::getAsJsonObject))
                    .forEach(store -> {
                        String address = store.get("address").getAsString();
                        String googleLink = Jsoup.parse(store.get("map").getAsString()).selectFirst("iframe[src]").attr("src");
                        saveDataWithHref(address, null, googleLink, "seoulspa_places");
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
