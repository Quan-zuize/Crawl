package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class HoangPhucCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(2, ChronoUnit.SECONDS));
        chromeDriver.get("https://hoang-phuc.com/danh-sach-cua-hang.html");
        Object result = chromeDriver.executeScript("return window.js_array");
        gson.fromJson(gson.toJson(result), JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonObject).forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("description").getAsString();
                    double lat = store.get("latitude").getAsDouble();
                    double lon = store.get("longitude").getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .name(name).address(address).lat(lat).lon(lon).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "hoang_phuc_places");
                });
    }
}
