package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MediaMartCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://mediamart.vn/he-thong-sieu-thi").get();
        Elements elements = document.select(".card-body > ul > li");
        elements.forEach(element -> {
            Element dataElement = element.select("a").first();
            String name = dataElement.text();
            String googleUrl = "";
            try {
                googleUrl = Jsoup.parse(dataElement.attr("data")).select("iframe").first().attr("src");
            } catch (Exception e) {
                System.out.println("Error url null: " + e.getMessage());
                return;
            }
            String address = element.select(".store-address").first().ownText();
            address = address.split("-")[0];
            saveDataWithHref(address, name, googleUrl, "media_mart_places");
        });
    }
}
