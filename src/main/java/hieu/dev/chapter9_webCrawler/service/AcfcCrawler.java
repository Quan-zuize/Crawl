package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class AcfcCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://www.acfc.com.vn/he-thong-cua-hang");
        Object result = chromeDriver.executeScript("return window.js_array");
        Map<String, JsonObject> coordinatesMap = gson.fromJson(gson.toJson(result), JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonObject)
                .collect(Collectors.toMap(store -> store.get("location_id").getAsString(), store -> store));

        chromeDriver.findElements(By.cssSelector(".li-location"))
                .forEach(store -> {
                    String id = store.getAttribute("item-id");
                    double lat = coordinatesMap.get(id).get("latitude").getAsDouble();
                    double lon = coordinatesMap.get(id).get("longitude").getAsDouble();
                    String name = store.findElement(By.className("locationTitle")).getText();
                    String address = store.findElement(By.className("mapAddressContent1")).getText();
                    BaseEntity entity = BaseEntity.builder()
                            .lon(lon).lat(lat).address(address).name(name).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "acfc_places");
                });
    }
}
