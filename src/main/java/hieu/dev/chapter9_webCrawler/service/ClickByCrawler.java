package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ClickByCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://clickbuy.com.vn/he-thong-cua-hang").get();
        Elements elements = document.select(".store-content > p");
        elements.forEach(element -> {
            String address = element.ownText();
            address = address.substring(1).replace("::", "").trim();
            String googleLink = Jsoup.parse(element.attr("data-map")).getElementsByTag("iframe").first().absUrl("src");
            if(googleLink.contains("3d!") || googleLink.contains("&ll=") || googleLink.contains("/@")) {
                saveDataWithHref(address, null, googleLink, "click_buy_places");
            } else {
                saveData(address, null, "click_buy_places");
            }
        });
    }
}
