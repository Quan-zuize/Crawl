package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Service
public class PVComBankCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://www.pvcombank.com.vn/mang-luoi").get();
        Elements locationLis = document.select("div[address]");
        for (Element locationLi : locationLis) {
            String name = locationLi.attr("title");

            String[] coordinates = locationLi.attr("lat").split(",");
            double lat, lon;

            if(coordinates.length == 2) {
                lat = Double.parseDouble(coordinates[0]);
                lon = Double.parseDouble(coordinates[1]);
            } else {
                lat = Double.parseDouble(locationLi.attr("lat"));
                lon = Double.parseDouble(locationLi.attr("lng"));
            }
            String address = locationLi.attr("address").split("- ĐT:")[0];

            BaseEntity entity = BaseEntity.builder()
                    .lon(lon).lat(lat).address(address).name(name).build();
            System.out.println(gson.toJson(entity));
            mongoTemplate.save(entity, "pv_com_bank_places");
        }
    }
}
