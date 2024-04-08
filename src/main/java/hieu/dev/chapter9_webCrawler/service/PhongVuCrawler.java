package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class PhongVuCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://phongvu.vn/p/he-thong-showroom-phong-vu").get();
        String data = document.getElementById("__NEXT_DATA__").html();
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        jsonObject.getAsJsonObject("props").getAsJsonObject("pageProps")
                .getAsJsonObject("pbContent").getAsJsonObject("OQjNwc3Ag3")
                .getAsJsonObject("customAttributes").getAsJsonObject("storelist")
                .getAsJsonArray("stores").asList().stream().map(JsonElement::getAsJsonObject).forEach(store -> {
                    String address = store.get("address").getAsString();
                    String googleLink = Jsoup.parse(store.get("map").getAsString()).getElementsByTag("iframe").attr("src");
                    saveDataWithHref(address, null, googleLink, "phong_vu_places");
                });
    }
}
