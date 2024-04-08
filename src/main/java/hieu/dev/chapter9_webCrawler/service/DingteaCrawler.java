package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DingteaCrawler extends BaseSeleniumCrawler {
//    @PostConstruct
    public void crawDingTea() throws IOException {
        driver.get("https://www.google.com/maps");
        Document document = Jsoup.connect("https://dingtea.vn/he-thong-cua-hang").get();
        Elements itemElements = document.getElementsByClass("stores-row").first().getElementsByTag("span");
        for(Element item : itemElements) {
            String address = item.text();
            saveData(address.split(":")[1], address, null, "dingtea_places");
        }
    }


}
