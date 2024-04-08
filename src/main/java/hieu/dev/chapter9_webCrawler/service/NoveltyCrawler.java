package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NoveltyCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://www.novelty.com.vn/he-thong-cua-hang").get();
        document.getElementsByClass("distributor-item")
                .forEach(element -> {
                    try {
                        String address = element.attr("data-address");
                        String onclick = element.attr("onclick");
                        if(Strings.isEmpty(address) || Strings.isEmpty(onclick)) return;

//                        System.out.println(onclick);
                        String googleLink = onclick.split("'")[1].trim();
                        if(Strings.isEmpty(googleLink)) return;
                        saveDataWithHref(address, null, googleLink, "novelty_places");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
    }
}
