package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class VietTienCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://www.viettien.com.vn/vi/he-thong-cua-hang");
        Object data = null;
        while (Objects.isNull(data)) {
            data = chromeDriver.executeScript("return sessionStorage.shops");
            Utils.sleep(100);
        }
        gson.fromJson(data.toString(), JsonObject.class)
                .getAsJsonArray("shops")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    try {
                        String address = store.get("address").getAsString();
                        if(address.split("-").length > 1) {
                            address = address.split("-")[1];
                        }
                        if(store.get("lat_lng").getAsString().isEmpty()) return;
                        String[] coordinates = store.get("lat_lng").getAsString().split(",");
                        double lat = Double.parseDouble(coordinates[0].trim());
                        double lng = Double.parseDouble(coordinates[1].trim());
                        BaseEntity entity = BaseEntity.builder()
                                .address(address).lat(lat).lon(lat).build();
                        mongoTemplate.save(entity, "viet_tien_places");
                    } catch (Exception e) {
                        System.out.println(store.get("lat_lng").getAsString() + ": " + e.getMessage());
                    }
                });
    }
}
