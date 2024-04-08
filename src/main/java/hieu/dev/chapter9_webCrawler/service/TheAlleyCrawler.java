package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class TheAlleyCrawler extends BaseSeleniumCrawler {
    public void crawlTheAlley() throws IOException {
        Document document = Jsoup.connect("https://www.the-alley.vn/store.html").get();
        Elements storeElements = document.getElementsByTag("ul");
        storeElements.stream().filter(storeElement -> !"store-list".equals(storeElement.attr("class")))
                .forEach(storeElement -> {
                    try {
                        String name = storeElement.getElementsByTag("p").first().text();
                        String address = storeElement.getElementsByClass("address").first().text();
                        address = address.replaceAll("(?i) address:\\s*", "");
                        String link = storeElement.selectFirst("a[href]").absUrl("href");
                        saveDataWithHref(address, name, link, "the_alley_places");
                    } catch (Exception e) {
                        log.error("Error: {}", e.getMessage(), e);
                    }
                });
    }
}
