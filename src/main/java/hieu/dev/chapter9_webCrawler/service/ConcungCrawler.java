package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class ConcungCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://concung.com/tim-sieu-thi.html");
        Object result = chromeDriver.executeScript("return storeall");
        gson.fromJson(gson.toJson(result), JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("address").getAsString();
                    double lat = store.get("latitude").getAsDouble();
                    double lon = store.get("longitude").getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .name(name).address(address).lon(lon).lat(lat).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "concung_places");
                });
    }
}
