package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class HoanKiem360Crawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://hoankiem360.vn/ban_do").get();
        String data = document.html().split("locations =")[1].split("];")[0] + ']';
        gson.fromJson(data, JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonArray)
                .forEach(place -> {
                    Document attrDocument = Jsoup.parse(place.get(0).getAsString());
                    try {
                        double lat = place.get(1).getAsDouble();
                        double lon = place.get(2).getAsDouble();
                        String name = attrDocument.selectFirst("a[href]").text();

                        String address = attrDocument.body().ownText();
                        if(address.split("Địa chỉ:").length > 1) {
                            address = address.split("Địa chỉ:")[1].trim();
                        } else {
                            address = null;
                        }

                        BaseEntity entity = BaseEntity.builder()
                                .lat(lat).lon(lon).name(name).address(address).build();
                        log.info("Place: {}", entity);
                        mongoTemplate.save(entity, "hoankiem360_places");
                    } catch (Exception e) {
                        log.error("Error while handle document: {}", attrDocument);
                    }
                });
    }
}
