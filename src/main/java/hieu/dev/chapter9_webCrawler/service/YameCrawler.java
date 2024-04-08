package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class YameCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://yame.vn/tim-cua-hang").get();
        document.select(".panel-default")
                .forEach(store -> {
                    String name = store.selectFirst(".panel-title").text();
                    String address = store.selectFirst(".adr-info p").text();
                    String googleLink = store.selectFirst(".adr-info a[href]").attr("href");
                    saveDataWithHref(address, name, googleLink, "yame_places");
                });
    }
}
