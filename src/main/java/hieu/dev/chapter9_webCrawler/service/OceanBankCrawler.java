package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Service
public class OceanBankCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        driver.get("https://oceanbank.vn/mang-luoi.html");
        Object o = driver.executeScript("return markers");
        List<Map<String, String>> elements = gson.fromJson(gson.toJson(o), new TypeToken<List<Map<String, String>>>(){}.getType());
        elements.stream().map(dataMap -> {
            BaseEntity entity = new BaseEntity();
            entity.setName(dataMap.get("fullname"));
            entity.setAddress(dataMap.get("address"));
            entity.setLat(Double.valueOf(dataMap.get("lat")));
            entity.setLon(Double.valueOf(dataMap.get("lng")));
            entity.setId(dataMap.get("lat") + "," + dataMap.get("lng"));
            return entity;
        }).forEach(entity -> mongoTemplate.save(entity, "ocean_bank_places"));
    }
}
