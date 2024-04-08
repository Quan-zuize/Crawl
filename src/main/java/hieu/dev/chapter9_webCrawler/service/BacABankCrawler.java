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
public class BacABankCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://baca-bank.vn/SitePages/website/mang-luoi.aspx").get();
        Elements locationLis = document.getElementsByClass("content").first().getElementsByTag("li");
        for (Element locationLi : locationLis) {
            Element aElement = locationLi.getElementsByTag("a").first();
            String id = aElement.id();
            String name = aElement.text();
            String[] coordinates = aElement.attr("onclick").split("\\(")[1].split("\\)")[0].split(",");
            double lat = Double.parseDouble(coordinates[0].trim());
            double lon = Double.parseDouble(coordinates[1].trim());
            String address = locationLi.text().replace(name, "");

            BaseEntity entity = BaseEntity.builder()
                    .id(id).lon(lon).lat(lat).address(address).name(name).build();
            System.out.println(gson.toJson(entity));
            mongoTemplate.save(entity, "bac_a_bank");
        }
    }
}
