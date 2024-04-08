package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AnhKhoaBakeryCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://anhhoabakery.vn/pages/he-thong-cua-hang").get();
        document.select("div.content-page > div")
                .forEach(store -> {
                    String name = store.selectFirst("h3").text();
                    String address = store.selectFirst(".contact_ht").ownText();
                    String googleLink = store.selectFirst("iframe[src]").attr("src");
                    saveDataWithHref(address, name, googleLink, "anh_khoa_bakery_places");
                });
    }
}
