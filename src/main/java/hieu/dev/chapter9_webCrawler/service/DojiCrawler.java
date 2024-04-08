package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DojiCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://doji.vn/he-thong-phan-phoi").get();
        document.select(".the-content > p").not("p[style]")
                .stream().map(Element::text)
                .filter(store -> store.contains("*"))
                .forEach(store -> {
                    String name = store.split(",")[0].replace("*", "").trim();
                    String address = store.replace("*","").split(". ĐT:")[0].trim();
                    if(name.contains("DOJI")) {
                        address = address.replace(name + ",", "");
                    } else {
                        name = null;
                    }
                    saveData(address, name, "doji_places");
                });
    }
}
