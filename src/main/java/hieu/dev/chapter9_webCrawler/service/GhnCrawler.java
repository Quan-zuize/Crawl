package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GhnCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://ghn.vn/blogs/he-thong-buu-cuc").get();
        String googleDataLink = document.selectFirst("iframe[src]").attr("src");
        saveGoogleDataLinkSelenium(googleDataLink, "ghn_places");
    }
}
