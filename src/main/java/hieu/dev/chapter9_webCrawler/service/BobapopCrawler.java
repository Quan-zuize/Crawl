package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Slf4j
@Service
public class BobapopCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://bobapop.com.vn/").get();
        String data = HtmlEscape.unescapeHtml(document.html().split("myLocations =")[1].split("];")[0]).trim() + ']';
        gson.fromJson(data, JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonArray)
                .forEach(store -> {
                    try {
                        String address = store.get(0).getAsString() + ", " + Jsoup.parse(store.get(3).getAsString()).text();
                        double lat = store.get(1).getAsDouble();
                        double lon = store.get(2).getAsDouble();
                        BaseEntity entity = BaseEntity.builder()
                                .address(address).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "bobapop_places");
                    } catch (Exception e) {
                        log.error("Error while crawl: {}", store);
                    }

                });
    }


}
