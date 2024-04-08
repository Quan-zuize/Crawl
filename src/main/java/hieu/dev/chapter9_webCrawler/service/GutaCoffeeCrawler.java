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
public class GutaCoffeeCrawler extends BaseSeleniumCrawler {
    public void crawlGutaCoffee() throws IOException {
        Document document = Jsoup.connect("https://gutacafe.com/cua-hang").get();
        Elements storeDivs = document.getElementsByClass("first:border-t-0");
        storeDivs.forEach(storeDiv -> {
            try {
                Elements data = storeDiv.getElementsByTag("p");
                String name = data.get(0).text();
                String address = data.get(1).text();
                saveData(address, name, "guta_places");
            } catch (Exception e) {
                log.error("Error while handle guta coffee: {}", storeDiv, e);
            }
        });
    }
}
