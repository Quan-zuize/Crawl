package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BiluxuryCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://biluxury.vn/pages/he-thong-cua-hang").get();
        document.select(".address-detail li[data-tinh]")
                .forEach(store -> {
                    String name = store.selectFirst("div.title").text();
                    String address = store.selectFirst("div.desc > p").text();
                    String googleLink = store.selectFirst("div.desc a[href]").attr("href");
                    if(!googleLink.contains("maps")) return;
                    saveDataWithHref(address, name, googleLink, "biluxury_places");
                });
    }
}
