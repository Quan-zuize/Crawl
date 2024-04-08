package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class KimNgocThuyCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://kimngocthuy.com/he-thong-cua-hang").get();
        document.getElementsByClass("infomation-footer-item")
                .forEach(store -> {
                    String address = store.getElementsByClass("address").first().text();

                    Element googleLinkElement = Jsoup.parse(store.attr("data-map")).selectFirst("iframe[data-litespeed-src]");
                    address = address.split(":")[1];
                    String googleLink = Objects.nonNull(googleLinkElement) ?
                            googleLinkElement.attr("data-litespeed-src") :
                            store.selectFirst("a[href]").attr("href");
                    saveDataWithHref(address, null, googleLink, "kim_ngoc_thuy_places");
                });
    }
}
