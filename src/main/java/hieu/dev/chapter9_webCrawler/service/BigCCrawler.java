package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class BigCCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://www.bigc.vn/store.html").get();
        String stores = document.getElementById("stores").attr("data-stores");
        gson.fromJson(stores, JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonArray)
                .forEach(store -> {
                    double lat = store.get(1).getAsDouble();
                    double lon = store.get(2).getAsDouble();
                    String name = store.get(3).getAsString();
                    String address = store.get(4).getAsString();
                    BaseEntity entity = BaseEntity.builder()
                            .lat(lat).lon(lon).address(address).name(name).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "big_c_places");
                });
    }
}
