package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HMCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.geolocation", 2);
        options.setExperimentalOption("prefs", prefs);
        ChromeDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.get("https://www2.hm.com/vi_vn/customer-service/shopping-at-hm/store-locator");
        try {
            chromeDriver.findElement(By.id("onetrust-accept-btn-handler")).click();
        } catch (Exception ignored) {
        }
        int size = getListStores(chromeDriver).size();
        while (size == 0) {
            size = getListStores(chromeDriver).size();
            Utils.sleep(1000);
        }
        for (int i = 0; i < size; i++) {
            chromeDriver.executeScript(String.format("return $(\".gm-style div[role='button']\")[%d].click()", i));
            WebElement attrElement = chromeDriver.findElement(By.cssSelector("dl[data-testid]"));
            String name = attrElement.findElement(By.tagName("button")).getText();
            String address = attrElement.findElement(By.cssSelector("address > span")).getText();
            String googleLink = attrElement.findElement(By.cssSelector("a[href]")).getAttribute("href");

            saveDataWithHref(address, name, googleLink, "hm_places");
        }
    }

    private static List<WebElement> getListStores(ChromeDriver chromeDriver) {
        return chromeDriver.findElements(By.cssSelector(".gm-style div[role='button']"));
    }
}
