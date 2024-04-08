package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v118.network.Network;
import org.openqa.selenium.json.Json;
import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class PicoCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        DevTools devTools = devTools();
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            String url = responseReceived.getResponse().getUrl();
            if (!url.contains("https://ecommerce.pico.vn/user/api/store")) {
                return;
            }
            String body = devTools.send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
            System.out.println(body);
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
            JsonArray data = jsonObject.getAsJsonArray("data");
            data.asList().stream().map(JsonElement::getAsJsonObject).forEach(store -> {
                String name = store.getAsJsonObject("name").get("vi").getAsString();
                String address = store.get("fullAddress").getAsString();
                double lat = store.get("lat").getAsDouble();
                double lon = store.get("lng").getAsDouble();
                BaseEntity baseEntity = BaseEntity.builder()
                        .lat(lat).lon(lon).name(name).address(address).build();
                System.out.println(baseEntity);
                mongoTemplate.save(baseEntity, "pico_places");
            });
        });
        driver.get("https://pico.vn/store");
    }
}
