package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class ToranoCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://torano.vn/pages/he-thong-cua-hang");
        String urlMap = chromeDriver.executeScript("return urlMap").toString();

        ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(urlMap, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
        String body = response.getBody();
        gson.fromJson(body, JsonObject.class).getAsJsonArray("cuahang")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("shop_name").getAsString();
                    String address = store.get("address").getAsString();
                    String googleLink = Jsoup.parse(store.get("map").getAsString()).getElementsByTag("iframe").first().attr("src");
                    System.out.println(name + address + ": " + googleLink);
                    saveDataWithHref(address, name, googleLink, "torano_places");
                });
    }
}
