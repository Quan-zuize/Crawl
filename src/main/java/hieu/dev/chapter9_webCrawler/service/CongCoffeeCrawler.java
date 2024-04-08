package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.CongCoffeeEntity;
import hieu.dev.chapter9_webCrawler.entity.PhucLongEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Service
@Slf4j
public class CongCoffeeCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

//    @PostConstruct
    public void crawlCongCoffee() throws IOException {
        driver.get("https://www.google.com/maps");
        Connection connect = Jsoup.connect("https://congcaphe.com/stores");
        Document document = connect.get();
        Elements cityElements = document.select("li > a[href]");
        List<String> cityUrls = cityElements.stream().map(cityElement -> cityElement.absUrl("href")).toList();
        for (String cityUrl : cityUrls) {
            Document districtDocument = Jsoup.connect(cityUrl).get();
            Elements districtElements = districtDocument.select("li > a[href]");
            List<String> districtUrls = districtElements.stream().map(cityElement -> cityElement.absUrl("href")).toList();
            for (String districtUrl : districtUrls) {
                Document storeDocument = Jsoup.connect(districtUrl).get();
                String address = storeDocument.selectFirst("div[class='sidebar']").getElementsByClass("content").first().text();
                String currentUrl = Utils.getSearchUrl(driver, address);

                List<Double> coordinates = Utils.handleCoordinatesByUrl(currentUrl);
                if (!CollectionUtils.isEmpty(coordinates)) {
                    double lat = coordinates.get(0);
                    double lon = coordinates.get(1);

                    CongCoffeeEntity entity = new CongCoffeeEntity();
                    entity.setAddress(address);
                    entity.setLat(lat);
                    entity.setLon(lon);
                    mongoTemplate.save(entity);
                    log.info("Place: {}", gson.toJson(entity));
                }
                Utils.sleep(200);
            }
        }
    }
}
