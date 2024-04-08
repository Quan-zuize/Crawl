package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class SoiBienCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://soibien.vn/he-thong-cua-hang");
        String data = chromeDriver.findElement(By.id("__NEXT_DATA__")).getAttribute("innerHTML");
        gson.fromJson(data, JsonObject.class).getAsJsonObject("props")
                .getAsJsonObject("pageProps").getAsJsonObject("pbContent")
                .getAsJsonObject("Mc9yOHyAH0").getAsJsonObject("customAttributes")
                .getAsJsonObject("storelist").getAsJsonArray("stores")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("address").getAsString();
                    String googleLink = Jsoup.parse(store.get("map").getAsString()).getElementsByTag("iframe").first().attr("src");
                    saveDataWithHref(address, name, googleLink, "soibien_places");
                });
    }
}
