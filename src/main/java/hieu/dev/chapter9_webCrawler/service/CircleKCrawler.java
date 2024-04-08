package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class CircleKCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://www.circlek.com.vn/vi/he-thong-circle-k/");
        chromeDriver.findElement(By.id("load_store")).findElements(By.className("item")).forEach(webElement -> {
            String address = webElement.findElement(By.tagName("p")).getAttribute("innerHTML").replace("<br>", ", ");
            double lat = Double.parseDouble(webElement.findElement(By.cssSelector("a[data-index]")).getAttribute("data-lat"));
            double lon = Double.parseDouble(webElement.findElement(By.cssSelector("a[data-index]")).getAttribute("data-lng"));

            BaseEntity entity = BaseEntity.builder().address(address).lon(lon).lat(lat).build();
            System.out.println(entity);
            mongoTemplate.save(entity, "circle_k_places");
        });
    }
}
