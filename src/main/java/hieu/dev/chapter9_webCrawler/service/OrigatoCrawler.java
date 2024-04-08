package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrigatoCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://origato.com.vn/he-thong-cua-hang-origato/").get();
        document.select("ul.tab-list > li")
                .forEach(store -> {
                    String nameAddress = store.ownText();
                    String name = nameAddress.split("[:\\-–]")[0].trim();
                    String address = nameAddress.split("[:\\-–]")[1].trim();
                    String googleLink = store.attr("data-src");
                    saveDataWithHref(address, name, googleLink, "origato_places");
                });
    }
}
