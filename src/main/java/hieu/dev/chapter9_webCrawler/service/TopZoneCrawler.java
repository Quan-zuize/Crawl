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
public class TopZoneCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://www.topzone.vn/").get();
        Elements elements = document.select(".store-list").first().select("p");
        elements.forEach(element -> {
            String href = element.select("a[href]").first().absUrl("href");
            try {
                Document detailDocument = Jsoup.connect(href).get();
                String name = detailDocument.select("h1").text();
                String address = detailDocument.select(".address > span").first().text();
                String googleUrl = detailDocument.select(".viewstreet").first().absUrl("href");
                saveDataWithHref(address, name, googleUrl, "top_zone_places");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
    }
}
