package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class GPBankCrawler extends BaseSeleniumCrawler {
    public void crawlGPBank() throws IOException {
        Document document = Jsoup.connect("https://www.gpbank.com.vn/Network").get();
        Elements locationDivs = document.select("tr[style=\"font-size:14px;\"]");
        for (Element locationDiv : locationDivs) {
            Elements attrElements = locationDiv.getElementsByTag("td");
            String name = attrElements.get(0).text();
            String address = attrElements.get(1).text();
            saveData(address, name, "gp_bank_places");
        }
    }
}
