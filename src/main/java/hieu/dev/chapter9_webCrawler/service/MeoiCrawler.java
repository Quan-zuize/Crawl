package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MeoiCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://meoi.com.vn/pages/he-thong-cua-hang").get();
        Elements items = document.getElementsByClass("address-detail").first().getElementsByTag("a");
        items.forEach(item -> {
            String name = item.ownText();
            String address = item.getElementsByTag("span").first().text();
            try {
                address = address.split("Hotline:")[0];
                address = address.split("Địa chỉ:")[1].trim();
            } catch (Exception ignored) {}
            saveData("Mẹ ơi" + "," + address, address, name, "meoi_places");
        });
    }
}
