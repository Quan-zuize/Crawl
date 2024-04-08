package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Service
public class Gs25Crawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        for (int page = 1; ; page++) {
            String url = String.format("https://gs25.com.vn/store/?p=%d&city=0&district=0&service=0&criteria=0&address=", page);
            System.out.println(url);
            ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
            Document document = Jsoup.parse(Objects.requireNonNull(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody())));
            if (document.select(".box-store-list .item").isEmpty()) return;
            document.select(".box-store-list .item").forEach(store -> {
                String name = store.getElementsByClass("left").text();
                String address = store.getElementsByClass("right").first().getElementsByTag("p").first().text();
                String latStr = store.select("a[data-group='viewMap']").attr("data-latitude");

                if(Strings.isEmpty(latStr)) return;
                double lat = Double.parseDouble(latStr.contains(",") ? latStr.split(",")[0].trim() : latStr);
                double lon = Double.parseDouble(store.select("a[data-group='viewMap']").attr("data-longitude"));
                BaseEntity entity = BaseEntity.builder().name(name).address(address).lon(lon).lat(lat).build();
                System.out.println(entity);
                mongoTemplate.save(entity, "gs25_places");
            });
        }


    }
}
