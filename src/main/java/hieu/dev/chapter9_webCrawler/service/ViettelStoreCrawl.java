package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ViettelStoreCrawl extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String collectionName = "viettel_store_places";
        String url = "https://viettelstore.vn/AjaxAction.aspx?action=get-markets&provinceId=-1&districtId=-1&shopId=3";
        HttpHeaders httpHeaders = new HttpHeaders(BaseHttpClient.headers);
        httpHeaders.set("x-requested-with", "XMLHttpRequest");
        httpHeaders.set("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>("", httpHeaders), String.class);
        Document document = Jsoup.parse(Objects.requireNonNull(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody())));
        Elements elements = document.getElementsByClass("item market");
        elements.forEach(element -> {
            Element dataElement = element.getElementsByTag("i").first();
            String name = dataElement.attr("data-name");
            String address = element.getElementsByTag("a").text();
            if(Strings.isEmpty(dataElement.attr("data-latitude"))) {
                saveData(address, name, collectionName);
                return;
            }

            Double lat = Double.parseDouble(dataElement.attr("data-latitude"));
            Double lon = Double.parseDouble(dataElement.attr("data-longtitude"));
            BaseEntity baseEntity = BaseEntity.builder()
                    .address(address).name(name).lon(lon).lat(lat).build();
            mongoTemplate.save(baseEntity, collectionName);
            System.out.println(baseEntity);
        });
    }
}
