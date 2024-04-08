package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.McDonaldEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class McDonaldCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
//    @PostConstruct
    public void crawlMcDonald() throws IOException {
        driver.get("https://www.google.com/maps");
        List<String> tinhIds = getTinhIds();
        String tinhId = "0";
        Connection connect = Jsoup.connect(String.format("https://mcdonalds.vn/cua-hang/%s-0/", tinhId));
        Document document = connect.get();
        Elements storeDivs = document.getElementsByClass("tbox-address-store");
        List<McDonaldEntity> entities = storeDivs.stream().map(McDonaldEntity::from).filter(Objects::nonNull).toList();
        log.info("Handle {}, size {}", tinhId, entities.size());
        entities.forEach(mongoTemplate::save);
    }

    private static List<String> getTinhIds() throws IOException {
        Connection connect = Jsoup.connect("https://mcdonalds.vn/cua-hang/0-0/");
        Document document = connect.get();
        Element tinhSelectElement = document.getElementById("tinh_id");
        Elements tinhElements = tinhSelectElement.getElementsByTag("option");
        tinhElements.remove(0);
        return tinhElements.stream().map(Element::val).toList();
    }
}
