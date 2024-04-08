package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HuyThanhJewelryCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://www.huythanhjewelry.vn/pages/he-thong-cua-hang-2022").get();
        document.select(".address-detail li")
                .forEach(store -> {
                    String address = store.selectFirst(".desc > p").text();
                    address = address.split(":")[1].trim();
                    String googleLink = store.selectFirst(".desc a[target][href]").attr("href");
                    saveDataWithHref(address, null, googleLink, "huythanh_jewelry_places");
                });
    }
}
