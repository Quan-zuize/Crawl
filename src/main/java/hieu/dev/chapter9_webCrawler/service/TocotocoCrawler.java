package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class TocotocoCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
//    @PostConstruct
    public void crawTocotoco() throws IOException {
        driver.get("https://www.google.com/maps");
        Document document = Jsoup.connect("https://tocotocotea.com/stores/").get();
        Elements itemElements = document.getElementsByClass("itemStore");
        for(Element item : itemElements) {
            String address = item.attr("data-filter");
            String name = item.select("a > span").first().text();
            saveData(address, name, "tocotoco_places");
        }
    }
}
