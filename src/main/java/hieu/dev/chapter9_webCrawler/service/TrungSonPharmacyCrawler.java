package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.TrungSonPharmacyEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class TrungSonPharmacyCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

//    @PostConstruct
    public void crawlTrungSon() throws IOException {
        Connection connect = Jsoup.connect("https://trungsoncare.com/pages/he-thong-cua-hang");
        Document document = connect.get();
        Elements storeDivs = document.select("div[datatinh][dataurl]");
        storeDivs.stream().map(item -> TrungSonPharmacyEntity.from(driver, item)).forEach(mongoTemplate::save);
    }
}
