package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class YodyCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        driver.get("https://yody.vn/he-thong-cua-hang");
        driver.findElements(By.cssSelector("#list-store > li"))

                .forEach(store -> {
                    String name = store.findElement(By.tagName("b")).getText();
                    String address = store.findElement(By.tagName("p")).getText();
                    String dataMap = store.getAttribute("data-map");
                    String googleLink = Jsoup.parse(dataMap).select("iframe").first().attr("src");
                    saveDataWithHref(address, name, googleLink, "yody_places");
                });
    }
}
