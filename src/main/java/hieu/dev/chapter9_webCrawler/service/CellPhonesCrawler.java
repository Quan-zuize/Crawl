package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CellPhonesCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://cellphones.com.vn/dia-chi-cua-hang");
        List<WebElement> storeTypes = chromeDriver.findElements(By.cssSelector(".boxOptions-stores label"));

        storeTypes.remove(0);
        storeTypes.forEach(storeTypeElement -> {
            retryClick(chromeDriver, storeTypeElement);
            while (!chromeDriver.findElements(By.className("lds-ring")).isEmpty()) {
                Utils.sleep(200);
            }
            chromeDriver.findElement(By.cssSelector(".boxMap-store-showall")).click();
            Utils.sleep(2000);
            chromeDriver.findElements(By.className("boxMap-store")).forEach(storeElement -> {
                String address = storeElement.findElement(By.tagName("p")).getText();
                saveData(address, null,"cell_phones_places");
            });
        });
    }
    private void retryClick(ChromeDriver chromeDriver, WebElement storeTypeElement) {
        while (true) {
            try {
                storeTypeElement.click();
                return;
            } catch (Exception e) {
                try {
                    chromeDriver.findElement(By.className("cancel-button")).click();
                } catch (Exception ignored){}
                Utils.sleep(100);
            }
        }
    }
}
