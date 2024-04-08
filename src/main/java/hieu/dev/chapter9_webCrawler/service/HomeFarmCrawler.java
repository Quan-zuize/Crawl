package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class HomeFarmCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://homefarm.vn/pages/he-thong-cua-hang").get();
        String url = document.select("section[data-json][data-plugin-showroom]").attr("data-json");
//        String url = "https://file.hstatic.net/1000301274/file/data_c46fc2706a6c416ea2081d1b1abfba74.json";
        String response = BaseHttpClient.restTemplate.getForObject(url, String.class);
        gson.fromJson(response, JsonObject.class).getAsJsonArray("data").asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("address").getAsString();
                    String googleLink = Jsoup.parse(store.get("google_map").getAsString()).getElementsByTag("iframe").first().attr("src");
                    saveDataWithHref(address, name, googleLink, "homefarm_places");
                });
    }
}
