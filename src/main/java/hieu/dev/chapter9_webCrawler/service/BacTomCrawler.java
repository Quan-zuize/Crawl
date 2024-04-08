package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BacTomCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://bactom.com/he-thong-cua-hang/").get();
        document.selectFirst(".section-content").select("span[style]")
                .stream().map(Element::ownText).forEach(address -> {
                    saveData("Bác Tôm " + address, address, null, "bactom_places");
                });
    }
}
