package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VuaNemCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://vuanem.com/stores").get();

        document.select("li.result-item")
                .forEach(store -> {
                    String name = store.getElementsByClass("heading").first().text();
                    String address = store.getElementsByClass("address").first().text();
                    address = address.split("Địa chỉ:")[1].split("Điện thoại:")[0].trim();

                    double lat = Double.parseDouble(store.selectFirst("p[data-latitude]").attr("data-latitude"));
                    double lon = Double.parseDouble(store.selectFirst("p[data-longitude]").attr("data-longitude"));
                    BaseEntity entity = BaseEntity.builder()
                            .lon(lon).lat(lat).address(address).name(name).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "vua_nem_places");
                });

    }
}
