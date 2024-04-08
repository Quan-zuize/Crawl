package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.unbescape.html.HtmlEscape;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class OwenCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        String url = UriComponentsBuilder.fromHttpUrl("https://owen.vn/amlocator/index/search/")
                .queryParam("country", "VN").toUriString();

        HttpHeaders httpHeaders = new HttpHeaders(BaseHttpClient.headers);
        httpHeaders.set("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpHeaders.set("x-requested-with", "XMLHttpRequest");
        ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>("", httpHeaders), String.class);
        String body = HtmlEscape.unescapeHtml(response.getBody());
        String html = gson.fromJson(body, JsonObject.class).get("html").getAsString();
        Document document = Jsoup.parse(html);
        System.out.println(document);

        document.select(".amlocator-store-desc").forEach(storeElement -> {
            Elements dataElements = storeElement.select("a[href]");
            String address = dataElements.get(0).text();
            String googleLink = dataElements.get(1).attr("href");
            try {
                String[] coordinates = googleLink.split("/")[googleLink.split("/").length - 1].split(",");
                double lat = Double.parseDouble(coordinates[0].trim());
                double lon = Double.parseDouble(coordinates[1].trim());
                BaseEntity entity = BaseEntity.builder()
                        .address(address).lat(lat).lon(lon).build();
                System.out.println(entity);
                mongoTemplate.save(entity, "owen_places");
            } catch (Exception e) {
                saveDataWithHref(address, null, googleLink, "owen_places");
            }
        });

    }
}
