package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.model.JsonRpcRequest;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Konni39Crawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        ChromeDriver driver = new ChromeDriver();
        driver.get("https://konni39.com/pages/he-thong-cua-hang");
        List<WebElement> elements = driver.findElements(By.className("shop-item"));
        elements.forEach(webElement -> {
            List<WebElement> attrs = webElement.findElements(By.tagName("input"));
            String id = attrs.get(0).getAttribute("value");
            String name = attrs.get(1).getAttribute("value");
            String address = webElement.findElement(By.cssSelector("p:nth-child(2)")).getText().trim();
            BaseEntity baseEntity = BaseEntity.builder()
                    .id(id).address(address).name(name).build();

            JsonRpcRequest request = new JsonRpcRequest(id);
            HttpHeaders httpHeaders = new HttpHeaders(headers);
            httpHeaders.set("content-type", "application/json");
            HttpEntity<String> httpEntity = new HttpEntity<>(gson.toJson(request), httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange("https://konni39.com/open_shop", HttpMethod.POST, httpEntity, String.class);
            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());

            JsonObject responseObj = gson.fromJson(body, JsonObject.class);
            String html = responseObj.getAsJsonObject("result").get("html_iframe_shop").getAsString();
            Document document = Jsoup.parse(html);
            String googleUrl = document.getElementsByTag("iframe").attr("abs:src");
            List<Double> coordinates = handleCoordinatesByUrlLocal(googleUrl);
            if(CollectionUtils.isEmpty(coordinates)) {
                String searchUrl = Utils.getSearchUrl(BaseSeleniumCrawler.driver, address);
                coordinates = Utils.getCoordinatesByUrl(BaseSeleniumCrawler.driver, searchUrl);
            }
            if(!CollectionUtils.isEmpty(coordinates)) {
                double lat = coordinates.get(0);
                double lon = coordinates.get(1);
                baseEntity.setLat(lat);
                baseEntity.setLon(lon);
                System.out.println(baseEntity);
                mongoTemplate.save(baseEntity, "konni39_places");
            } else {
                log.error("Error: {}-{}", googleUrl, baseEntity);
            }
        });
    }
    public static List<Double> handleCoordinatesByUrlLocal(String url) {
        if(Strings.isEmpty(url)) return null;
        try {
            Map<String, String> params = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().toSingleValueMap();
            String coordinateStr = params.get("q");
            String[] coordinate = coordinateStr.split(",");
            return List.of(Double.parseDouble(coordinate[0].trim()), Double.parseDouble(coordinate[1].trim()));
        } catch (Exception e) {
            return Utils.handleCoordinatesByUrl(url);
        }
    }
}
