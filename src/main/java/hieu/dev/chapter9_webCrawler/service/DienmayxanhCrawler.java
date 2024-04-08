package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DienmayxanhCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver driver = BaseSeleniumCrawler.driver;
        for (int page = 0; ; page++) {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.dienmayxanh.com/Store/SearchStoreByValue")
                    .queryParam("pageIndex", page).toUriString();
            System.out.println("Request: " + url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>("", headers), String.class);
            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
            if (Strings.isEmpty(body)) return;
            String html = gson.fromJson(body, JsonObject.class).get("html").getAsString();
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByTag("li");
            if(CollectionUtils.isEmpty(elements)) break;
            elements.forEach(element -> {
                if(element.className().equals("seemore")) return;
                String href = "https://www.dienmayxanh.com" + element.select("a[href]").first().attr("href");
                driver.get(href);
                String id = element.attr("data-id");
                String address = element.getElementsByTag("span").first().ownText();
                String name = driver.findElement(By.cssSelector(".almost > span")).getText();
                double lat = Double.parseDouble((String) driver.executeScript("return document.storeLat"));
                double lon = Double.parseDouble((String) driver.executeScript("return document.storeLng"));
                BaseEntity baseEntity = BaseEntity.builder()
                        .address(address).name(name).lon(lon).lat(lat).build();
                System.out.println(baseEntity);
                mongoTemplate.save(baseEntity, "dienmayxanh_places");
                Utils.sleep(100);
            });
        }

    }
}
