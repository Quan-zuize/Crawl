package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChicLandCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://chicland.vn/pages/showrooms");
        chromeDriver.findElements(By.cssSelector("#country > li")).forEach(store -> {
            try {
                if(Strings.isEmpty(store.getText())) return;
                List<WebElement> attrElements = store.findElements(By.cssSelector(".content p"));
                String name = attrElements.get(0).getText();
                String address = attrElements.get(1).getText();
                String googleUrl = store.findElement(By.cssSelector("iframe[src]")).getAttribute("src");
                saveDataWithHref(address, name, googleUrl, "chic_land_places");
            } catch (Exception e) {
                System.out.println(store.getText());
            }

        });
    }
}
