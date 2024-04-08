package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class AeonCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://www.aeon.com.vn/#branch");

        Utils.sleep(1000);
        chromeDriver.findElements(By.className("branch-content")).forEach(webElement -> {
            String googleUrl = webElement.findElement(By.className("google-map-static")).getAttribute("data-src");
            String center = UriComponentsBuilder.fromHttpUrl(googleUrl).build().getQueryParams().toSingleValueMap().get("center");
            System.out.println(center);
            if(center.equals(",")) return;
            String address = Utils.getAddress(driver, center);
            String[] coordinates = center.split(",");
            double lat = Double.parseDouble(coordinates[0]);
            double lon = Double.parseDouble(coordinates[1]);
            BaseEntity baseEntity = BaseEntity.builder().address(address).lon(lon).lat(lat).build();
            System.out.println(baseEntity);
            mongoTemplate.save(baseEntity, "aeon_places");
        });

    }
}
