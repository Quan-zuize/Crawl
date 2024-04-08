package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.AnKhangPharmacyEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.client.AnKhangPharmacyHttpClient.*;

@Service
@Slf4j
public class AnKhangPharmacyCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

//    @PostConstruct
    public void crawlAnKhang() throws IOException {
        List<String> cityCodes = getCityCodes();
        log.info("Get all cites size {}", cityCodes.size());
        cityCodes.forEach(cityCode -> {
            for (int page = 1; ; page++) {
                try {
                    String body = getDataPageByCityCode(cityCode, page);
                    JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
                    if (Objects.isNull(bodyObj)) return;

                    String html = bodyObj.getAsJsonObject("data").getAsJsonPrimitive("html").getAsString();
                    Document document = Jsoup.parse(html);
                    Elements liList = document.getElementsByTag("li");

                    List<AnKhangPharmacyEntity> entities = liList.stream().map(AnKhangPharmacyEntity::from).toList();
                    entities.forEach(mongoTemplate::save);
                    log.info("Insert all {}, size {}", cityCode, entities.size());

                    JsonPrimitive leftPrimitive = bodyObj.getAsJsonObject("data").getAsJsonPrimitive("left");
                    if (Objects.isNull(leftPrimitive) || leftPrimitive.getAsInt() <= 0) {
                        break;
                    }
                } catch (Exception e) {
                    log.info("Error while handle city {}: {}", cityCode, e.getMessage(), e);
                } finally {
                    Utils.sleep(300);
                }
            }
        });
    }
}
