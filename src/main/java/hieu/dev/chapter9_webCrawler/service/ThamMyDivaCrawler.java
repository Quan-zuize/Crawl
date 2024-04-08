package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ThamMyDivaCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://vienthammydiva.vn/chuoi-co-so/").get();
        document.getElementsByClass("chinhanh")
                .forEach(store -> {
                    String name = store.getElementsByTag("h3").first().text();
                    String address = store.getElementsByClass("content").first().text();
                    String href = store.selectFirst(".color-text[href]").attr("href");
                    if(Strings.isEmpty("href")) {
                        saveData(address, name, "tham_my_diva_places");
                    } else {
                        saveData(address, name, href, "tham_my_diva_places");
                    }
                });
    }
}
