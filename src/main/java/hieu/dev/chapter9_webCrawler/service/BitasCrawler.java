package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BitasCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://bitas.com.vn/cua-hang-bitas").get();
        document.select("div.box-cuahang-item div.grid-70")
                .forEach(store -> {
                    String name = store.selectFirst(".ten").text();
                    String address = store.selectFirst(".color2").text();
                    if(address.split(":").length > 1) {
                        address = address.split(":")[1];
                    }
                    saveData(address, name, "bitas_places");
                });
    }
}
