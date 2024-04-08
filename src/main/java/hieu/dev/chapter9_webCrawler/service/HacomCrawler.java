package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HacomCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://hacom.vn/showroom").get();
        Elements elements = document.select(".tab-content");
        elements.forEach(element -> {
            Element addressDetail = element.getElementsByClass("address-detail").first();
            String name = addressDetail.select("span").first().ownText();
            String address = addressDetail.select("span").get(1).ownText();
            String googleUrl = element.getElementsByTag("iframe").first().attr("src");
            saveDataWithHref(address, name, googleUrl, "hacom_places");
        });
    }
}
