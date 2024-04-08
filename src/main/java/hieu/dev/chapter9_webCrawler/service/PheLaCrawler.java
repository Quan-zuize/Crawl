package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PheLaCrawler extends BaseSeleniumCrawler {
    public void crawlPheLa() throws IOException {
        Document document = Jsoup.connect("https://phela.vn/he-thong-cua-hang/").get();
        Elements storeElements = document.getElementsByClass("location-name");
        storeElements.forEach(storeElement -> {
            String link = Jsoup.parse(storeElement.attr("data-iframe")).getElementsByTag("iframe").attr("abs:src");
            String address = storeElement.getElementsByTag("p").first().text();
            saveDataWithHref(address, null, link, "phe_la_places");
        });
    }
}
