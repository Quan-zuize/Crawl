package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.unbescape.html.HtmlEscape;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class EliseCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://elise.vn/maplist/");
        Object dataObj = chromeDriver.executeScript("return window.js_array");
        while (Objects.isNull(dataObj)) {
            dataObj = chromeDriver.executeScript("return window.js_array");
            Utils.sleep(2000);
        }
        String data = gson.toJson(dataObj);
        gson.fromJson(HtmlEscape.unescapeHtml(data), JsonArray.class).asList()
                .stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("detail_address").getAsString();
                    double lat = store.get("latitude").getAsDouble();
                    double lon = store.get("longitude").getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .address(address).name(name).lon(lon).lat(lat).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "elise_places");
                });
    }
}
