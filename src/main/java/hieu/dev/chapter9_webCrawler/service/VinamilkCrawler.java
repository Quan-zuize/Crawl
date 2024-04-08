package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VinamilkCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://new.vinamilk.com.vn/store-list").get();
        document.select("div.mb-1.none-break-inside").forEach(store -> {
            String name = store.getElementsByTag("span").first().text();
            String address = store.getElementsByTag("a").first().text();
            String googleUrl = store.getElementsByTag("a").first().attr("href");
            saveDataWithHref(address, name, googleUrl, "vina_milk_places");
        });
    }
}
