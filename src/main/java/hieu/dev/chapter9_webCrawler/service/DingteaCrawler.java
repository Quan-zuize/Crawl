package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BankEntity;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DingteaCrawler extends BaseSeleniumCrawler {
//    @PostConstruct
    public void crawDingTea() throws IOException {
        driver.get("https://www.google.com/maps");
        Document document = Jsoup.connect("https://dingtea.vn/he-thong-cua-hang").get();
        Elements stores = document.getElementsByClass("stores-cell");
        for(Element store : stores) {
            String address = store.select(".store-content > span").text().split(":\\s?")[1];
            Query query = new Query();
            query.addCriteria(Criteria.where("address").is(preHandle(address)));
            if(mongoTemplate.findOne(query, BaseEntity.class, "dingtea_places") != null){
                continue;
            }
            saveDataV2(address, "Ding Tea", "Ding Tea " + preHandle(address) + ", Hà Nội", "dingtea_places");
        }
    }


}
