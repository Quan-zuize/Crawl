package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AdamStoreCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://adamstorevn.com/pages/he-thong-cua-hang").get();
        document.select(".list-map-page").first().select("a[href][data-iframe]")
                .forEach(store -> {
                    String[] storeHtmls = store.html().split("\n");
                    String name = Jsoup.parse(storeHtmls[0]).text();
                    String address = Jsoup.parse(storeHtmls[1]).text();
                    String googleLink = Jsoup.parse(store.attr("data-iframe"))
                            .selectFirst("iframe[src]").attr("src");
                    saveDataWithHref(address, name, googleLink, "adam_store_places");
                });
    }
}
