package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class TinyStoreCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://tinistore.com/blogs/he-thong-cua-hang").get();
        Elements elements = document.select(".address-detail").first().getElementsByTag("li");
        elements.forEach(element -> {
            try {
                String name = element.getElementsByTag("b").text();
                String address = element.getElementsByTag("div").text();
                String href = element.getElementsByTag("a").attr("abs:href");
                HttpHeaders httpHeaders = new HttpHeaders(headers);
                httpHeaders.set("x-requested-with", "XMLHttpRequest");
                ResponseEntity<String> response = restTemplate.exchange(href + "?view=view-store", HttpMethod.GET, new HttpEntity<>("", httpHeaders), String.class);
                Document coordinateDocument = Jsoup.parse(Objects.requireNonNull(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody())));
                String googleUrl = coordinateDocument.getElementsByTag("iframe").attr("abs:src");
                List<Double> coordinates = Utils.handleCoordinatesByUrl(googleUrl);
                if(!CollectionUtils.isEmpty(coordinates)) {
                    double lat = coordinates.get(0);
                    double lon = coordinates.get(1);
                    BaseEntity baseEntity = BaseEntity.builder()
                            .name(name).address(address).lat(lat).lon(lon).build();
                    System.out.println(baseEntity);
                    mongoTemplate.save(baseEntity, "tiny_store_places");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        });
    }
}
