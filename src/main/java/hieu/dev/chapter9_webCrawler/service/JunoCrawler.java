package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Slf4j
@Service
public class JunoCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://juno.vn/blogs/he-thong-cua-hang");
        Object o = chromeDriver.executeScript("return locations");
        gson.fromJson(gson.toJson(o), JsonArray.class).asList()
                .stream().map(JsonElement::getAsJsonArray)
                .forEach(store -> {
                    String address = store.get(0).getAsString();
                    double lat = store.get(1).getAsDouble();
                    double lon = store.get(2).getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .address(address).lon(lon).lat(lat).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "juno_places");
                });
    }
}


