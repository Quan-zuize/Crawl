package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AristinoCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://aristino.com/cua-hang/").get();
        document.select(".select-list-address--item")
                .forEach(store -> {
                    String name = store.select(".info-adr > p:nth-child(1)").text();
                    String address = store.select(".info-adr > p:nth-child(2)").text();
                    saveData(address, name, "aristino_places");
                });
    }
}
