package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.unbescape.html.HtmlEscape;

import java.util.List;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class IvyModaCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.get("https://ivymoda.com/page/cuahang");
        int size = chromeDriver.findElements(By.cssSelector("ul.page-nav > li")).size();

        for (int i = 0; i < size; i++) {
            chromeDriver.findElements(By.cssSelector("ul.page-nav > li")).get(i).click();

            Object data = chromeDriver.executeScript("return list_shop_json");
            while (Strings.isEmpty(data.toString())) {
                data = chromeDriver.executeScript("return list_shop_json");
                Utils.sleep(1000);
            }
            List<JsonElement> stores = gson.fromJson(HtmlEscape.unescapeHtml(gson.toJson(data)), JsonArray.class).asList();
            stores.remove(0);
            stores.stream().map(JsonElement::getAsJsonObject).forEach(store -> {
                String name = store.get("name").getAsString();
                String address = store.get("address").getAsString();

                if (Objects.isNull(store.get("lat")) || store.get("lat").isJsonNull() || store.get("lat").getAsString().equals(",") || store.get("lat").getAsString().isEmpty())
                    return;
                String[] latParams = store.get("lat").getAsString().split(",");
                double lat, lon;
                if (latParams.length == 2) {
                    lat = Double.parseDouble(latParams[0]);
                    lon = Double.parseDouble(latParams[1]);
                } else {
                    lat = store.get("lat").getAsDouble();
                    lon = store.get("lng").getAsDouble();
                }

                BaseEntity entity = BaseEntity.builder()
                        .name(name).address(address).lon(lon).lat(lat).build();
                System.out.println(entity);
                mongoTemplate.save(entity, "ivy_moda_places");
            });
        }
    }
}
