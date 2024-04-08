package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class SjcCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        for (int page = 0; ; page++) {
            Document document = Jsoup.connect(String.format("https://sjc.com.vn/cua-hang-%d.html", page)).get();
            if(document.getElementsByClass("reveal-modal").isEmpty()) return;
            document.getElementsByClass("reveal-modal")
                    .forEach(store -> {
                        String name = store.getElementsByTag("h1").text();
                        String address = store.getElementsByTag("p").first().text();

                        if(Objects.nonNull(store.selectFirst("iframe[src]"))) {
                            String googleHref = store.selectFirst("iframe[src]").attr("src");
                            saveDataWithHref(address, name, googleHref, "sjc_places");
                        } else {
                            saveData(address, name, "sjc_places");
                        }
                    });
        }
    }
}
