package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.client.BaseHttpClient.headers;
import static hieu.dev.chapter9_webCrawler.client.BaseHttpClient.restTemplate;
import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class BitisCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://bitis.com.vn/pages/he-thong-cua-hang");
        Object storeData = chromeDriver.executeScript("return window.shop");
        String storeJson = gson.fromJson(gson.toJson(storeData), JsonObject.class).get("storeJson").getAsString();
        String url = "https:" + storeJson;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = response.getBody();
        gson.fromJson(body, JsonObject.class).getAsJsonArray("stores")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("address").getAsString();
                    if(Objects.isNull(store.get("lat")) || Strings.isEmpty(store.get("lat").getAsString())) {
                        saveData(address, name, "bitis_places");
                    } else {
                        double lat = store.get("lat").getAsDouble();
                        double lon = store.get("lng").getAsDouble();
                        BaseEntity entity = BaseEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "bitis_places");
                    }
                });
    }
}
