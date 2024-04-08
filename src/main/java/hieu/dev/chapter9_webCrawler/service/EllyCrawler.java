package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class EllyCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://elly.vn/he-thong-showroom/").get();
        String html = document.select("script[type='text/javascript']")
                .stream()
                .filter(data -> data.outerHtml().contains("showrooms"))
                .findFirst().get().html();
        String data = html.split("showrooms =")[1].split(";")[0];
        gson.fromJson(data, JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonObject)
                .filter(store -> !store.get("address").isJsonNull() && !store.get("address").getAsString().isEmpty())
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("address").getAsString();
                    String coordinates = store.get("toa_do").getAsString();
                    if(Strings.isEmpty(coordinates.split(",")[0])) {
                        saveData(address, name, "elly_places");
                    } else {
                        double lat = Double.parseDouble(coordinates.split(",")[0]);
                        double lon = Double.parseDouble(coordinates.split(",")[1]);
                        BaseEntity baseEntity = BaseEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon).build();
                        System.out.println(baseEntity);
                        mongoTemplate.save(baseEntity, "elly_places");
                    }

                });
    }
}
