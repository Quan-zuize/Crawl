package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class TigerSugarCrawler extends BaseSeleniumCrawler {
    public void crawlTigerSugar() throws IOException {
        Document document = Jsoup.connect("https://tigersugar.com.vn/he-thong-cua-hang").get();
        Elements addressElements = document.getElementsByClass("row").get(1).getElementsByTag("strong");
        addressElements.stream().map(Element::text).forEach(address -> {
            if(!address.contains("–")) return;
            saveData(address, null, "tiger_sugar_places");
        });
    }
}
