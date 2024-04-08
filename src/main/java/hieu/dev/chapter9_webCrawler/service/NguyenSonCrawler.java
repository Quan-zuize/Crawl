package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NguyenSonCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        try {
            Document document = Jsoup.connect("https://nguyenson.vn/pages/lien-he").get();
            String googleDataLink = document.selectFirst("p > iframe[src]").attr("src");
            System.out.println(googleDataLink);

            saveGoogleDataLink(googleDataLink,
                    address -> address.split("Bakery,")[1].split("ĐT:")[0].replace(".", "").trim(),
                    "nguyen_son_places"
                    );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
