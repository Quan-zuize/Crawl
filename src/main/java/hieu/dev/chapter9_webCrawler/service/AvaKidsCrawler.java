package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AvaKidsCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        for (int page = 1; ; page++) {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.avakids.com/Store/ViewMoreStoreHome")
                    .queryParam("pageIndex", page)
                    .queryParam("pageSize", "10")
                    .queryParam("type", "9")
                    .queryParam("isAjax", true).build().toUriString();
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
            if(Strings.isEmpty(body)) return;
            body = HtmlEscape.unescapeHtml(body);
            Document document = Jsoup.parse(body);
            Elements nameElements = document.select(".item > a");
            Elements addressElements = document.select(".txt");
            if(nameElements.isEmpty()) return;
            for (int i = 0; i < nameElements.size(); i++) {
                String[] idParams = nameElements.get(i).attr("href").split("-");
                String id = idParams[idParams.length - 1];
                String name = nameElements.get(i).text();
                String address = addressElements.get(i).getElementsByTag("p").text();
                String googleUrl = addressElements.get(i).getElementsByTag("a").attr("abs:href");
                List<Double> coordinates = handleCoordinatesByUrlLocal(googleUrl);
                if(Objects.nonNull(coordinates)) {
                    double lat = coordinates.get(0);
                    double lon = coordinates.get(1);
                    BaseEntity baseEntity = BaseEntity.builder()
                            .id(id).address(address).name(name).lat(lat).lon(lon).build();
                    System.out.println(baseEntity);
                    mongoTemplate.save(baseEntity, "avakids_places");
                }
            }
            Utils.sleep(100);
        }
    }
    public static List<Double> handleCoordinatesByUrlLocal(String url) {
        if(Strings.isEmpty(url)) return null;
        try {
            Map<String, String> params = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().toSingleValueMap();
            String coordinateStr = params.get("query");
            String[] coordinate = coordinateStr.split(",");
            return List.of(Double.parseDouble(coordinate[0].trim()), Double.parseDouble(coordinate[1].trim()));
        } catch (Exception e) {
            return Utils.handleCoordinatesByUrl(url);
        }
    }
}
