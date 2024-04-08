package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class May10Crawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://may10.vn/pages/showroom").get();
        String googleDataLink = document.select("iframe[src]").get(1).attr("src");
        saveGoogleDataLink(googleDataLink, address -> address.split("Địa chỉ:")[1], "may10_places");
    }

}
