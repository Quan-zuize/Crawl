package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class HDBankCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        driver.get("https://hdbank.com.vn/vi/atm-branch");
        driver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        handleListData("PGD");

        driver.findElement(By.className("tab-list")).findElements(By.tagName("li")).get(1).click();
        Utils.sleep(1000);
        handleListData("ATM");
    }

    private void handleListData(String placeCode) {
        List<WebElement> elements = driver.findElements(By.className("panel_list__item"));
        elements.forEach(webElement -> {
            try {
                WebElement titleElement = webElement.findElement(By.className("panel_list__item-title"));
                String name = titleElement.findElement(By.tagName("p")).getText();
                WebElement coordinateElement = titleElement.findElement(By.tagName("a"));
                double lat = Double.parseDouble(coordinateElement.getAttribute("lat"));
                double lon = Double.parseDouble(coordinateElement.getAttribute("lng"));

                String address = webElement.findElement(By.className("panel_list__item-desc")).findElement(By.tagName("p")).getText();
                BaseEntity baseEntity = BaseEntity.builder()
                        .address(address).name(name).lon(lon).lat(lat).placeCode(placeCode).build();
                System.out.println(baseEntity);
                mongoTemplate.save(baseEntity, "hd_bank_places");
            } catch (Exception e) {
                log.error("error: {}", e.getMessage());
            }
        });
    }
}
