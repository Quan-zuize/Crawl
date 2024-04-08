package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class GoVietnamCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://go-vietnam.vn/store.html").get();
        String stores = document.getElementById("stores").attr("data-stores");
        gson.fromJson(stores, JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonArray)
                .forEach(store -> {
                    String name = store.get(3).getAsString();
                    String address = store.get(5).getAsString();
                    System.out.println(name + ": " + address);
                    if(store.get(1).isJsonNull() || store.get(1).getAsString().isEmpty()) return;
                    double lat = store.get(1).getAsDouble();
                    double lon = store.get(2).getAsDouble();
                    BaseEntity entity = BaseEntity.builder()
                            .lat(lat).lon(lon).address(address).name(name).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "go_vietnam_places");
                });
    }
}
