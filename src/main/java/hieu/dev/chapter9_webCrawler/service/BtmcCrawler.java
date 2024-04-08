package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BtmcCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://btmc.vn/he-thong-phan-phoi.html").get();
        document.select("#MenuID > option")
                .stream().map(provinceElement -> provinceElement.absUrl("value"))
                .forEach(url -> {
                    try {
                        Document provinceDocument = Jsoup.connect(url).get();
                        Elements stores = provinceDocument.select(".tbale_htpp > tbody > tr");
                        stores.remove(0);
                        stores.forEach(store -> {
                            String name = store.select("div[align='left']").get(0).text();
                            String address = store.select("div[align='left']").get(1).text();
                            saveData(address, name, "btmc_places");
                        });
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
    }

}
