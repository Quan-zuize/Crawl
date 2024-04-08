package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class GoFoodCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://gofood.vn/chi-tiet-cua-hang").get();
        document.getElementById("list_store").getElementsByTag("li").forEach(element -> {
            try {
                String storeHref = element.select("a[href]").first().absUrl("href");
                Document storeDocument = Jsoup.connect(storeHref).get();
                String address = element.getElementsByClass("mb-2").get(1).text();
                String googleLink = storeDocument.select("iframe[src]").stream().map(iframe -> iframe.attr("src"))
                        .filter(link -> link.contains("https://www.google.com/maps")).findAny().orElse(null);
                saveDataWithHref(address, null, googleLink, "go_food_places");
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage(), e);
            }
        });

    }
}
