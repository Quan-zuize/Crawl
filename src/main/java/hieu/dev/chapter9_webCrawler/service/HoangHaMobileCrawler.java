package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HoangHaMobileCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://hoanghamobile.com/he-thong-cua-hang").get();
        Elements elements = document.select("div[class='info'] > p:nth-child(1)");
        elements.forEach(element -> {
            String address = element.text();
            saveData(address, null, "hoangha_mobile_places");
        });
    }
}
