package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MMVietNamCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://mmvietnam.com/danh-sach-he-thong/");
        List<WebElement> elements = chromeDriver.findElements(By.className("mm-list-location"));
        elements.forEach(storeElement -> {
            String name = storeElement.getAttribute("data-name");
            String address = storeElement.getAttribute("data-address");
            String[] coords = storeElement.getAttribute("data-coords").split(",");
            double lat = Double.parseDouble(coords[0]);
            double lon = Double.parseDouble(coords[1]);

            BaseEntity baseEntity = BaseEntity.builder()
                    .lat(lat).lon(lon).address(address).name(name).build();
            System.out.println(baseEntity);
            mongoTemplate.save(baseEntity, "mm_vietnam_places");
        });
    }
}
