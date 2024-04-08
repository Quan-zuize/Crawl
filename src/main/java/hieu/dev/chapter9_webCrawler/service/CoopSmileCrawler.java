package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CoopSmileCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://coopsmile.vn/pages/he-thong-cua-hang-co-opsmile").get();
        document.select(".ega-article-content li").forEach(element -> {
            String address = element.text();
            address = address.split("\\(")[0];
            String googleLink = element.select("a[href]").first().attr("href");
            if(!googleLink.contains("https://www.google.com/maps") && !googleLink.contains("https://goo.gl/maps")) return;
            saveDataWithHref(address, null, googleLink, "coop_smile_places");
        });
    }
}
