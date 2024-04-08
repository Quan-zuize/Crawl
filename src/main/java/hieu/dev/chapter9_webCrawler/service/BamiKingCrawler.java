package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BamiKingCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://bamiking.vn/lien-he/").get();
        String googleDataLink = document.selectFirst("iframe[src]").attr("src");
        saveGoogleDataLink(googleDataLink, address -> address, "bami_king_places");
    }
}
