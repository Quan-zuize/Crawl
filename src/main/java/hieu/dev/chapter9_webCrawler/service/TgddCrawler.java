package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TgddCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        List<String> listCity = getListCity();
        for (String provinceId : listCity) {
            for (int page = 0; ; page++) {
                String url = UriComponentsBuilder.fromHttpUrl("https://www.thegioididong.com/Store/SuggestSearch")
                        .queryParam("provinceId", provinceId)
                        .queryParam("districtId", 0)
                        .queryParam("pageIndex", page)
                        .queryParam("pageSize", 10)
                        .queryParam("type", -1)
                        .queryParam("loadAll", true).toUriString();
                HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
                if(Strings.isEmpty(body) || body.contains("Không tìm thấy siêu thị")) break;
                Document document = Jsoup.parse(body);
                Elements elements = document.getElementsByTag("li");
                int finalPage = page;
                elements.forEach(element -> {
                    String id = element.attr("data-id");
                    String address = element.getElementsByTag("a").text();
                    address = address.split("\\(")[0].trim();
                    String googleAddress = element.getElementsByTag("a").get(1).attr("abs:href");
                    List<Double> coordinates = handleCoordinatesByUrlLocal(googleAddress);
                    if(!CollectionUtils.isEmpty(coordinates)) {
                        double lat = coordinates.get(0);
                        double lon = coordinates.get(1);
                        BaseEntity baseEntity = BaseEntity.builder().address(address).lat(lat).lon(lon).build();
                        System.out.println(baseEntity);
                        mongoTemplate.save(baseEntity, "tgdd_places");
                    } else {
                        log.error("Error city, page, id, href: {} - {} - {} - {}", provinceId, finalPage, id, googleAddress);
                    }
                });
                Utils.sleep(100);
            }
        }


    }

    private static List<String> getListCity() throws IOException {
        Document document = Jsoup.connect("https://www.thegioididong.com/he-thong-sieu-thi-the-gioi-di-dong").get();
        Elements elements = document.getElementsByClass("stores-box").first().getElementsByTag("a");
        return elements.stream().map(element -> element.attr("data-value")).toList();
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
