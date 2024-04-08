package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class BibomartCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://bibomart.com.vn/maplist/index/").get();
        Elements items = document.getElementById("list_listitem").getElementsByClass("viewLocationDetail");
        for (Element item : items) {
            Elements addressElements = item.getElementsByTag("p");
            String address = addressElements.get(1).text();
            if (Strings.isEmpty(address)) {
                address = addressElements.get(2).text();
            }
            if(address.split("SĐT:").length > 1) {
                address = address.split("SĐT:")[0].trim();
            }
            saveData("Bibo Mart, " + address, address, null, "bibo_mart_places");
        }
    }
}
