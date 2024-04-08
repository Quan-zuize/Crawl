package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SevenamCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://sevenam.vn/pages/showrooms");
        chromeDriver.findElements(By.cssSelector("#address > li")).forEach(store -> {
            List<WebElement> spans = store.findElements(By.tagName("span"));
            String name = spans.get(0).getText();
            String address = spans.get(1).getText();

            String onclick = store.getAttribute("onclick");
            String[] coordinates = onclick.split("[,()]");
            if(Strings.isEmpty(coordinates[1])) return;
            double lat = Double.parseDouble(coordinates[1]);
            double lon = Double.parseDouble(coordinates[2]);
            BaseEntity entity = BaseEntity.builder()
                    .lat(lat).lon(lon).address(address).name(name).build();
            System.out.println(entity);
            mongoTemplate.save(entity, "sevenam_places");
        });
    }
}
