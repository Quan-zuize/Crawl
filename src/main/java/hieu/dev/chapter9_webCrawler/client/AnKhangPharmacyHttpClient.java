package hieu.dev.chapter9_webCrawler.client;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AnKhangPharmacyHttpClient extends BaseHttpClient {
    public static String getDataPageByCityCode(String cityCode, int page) {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.nhathuocankhang.com/Store/GetStoreList")
                .queryParam("ProvinceId", cityCode)
                .queryParam("IsViewMore", true)
                .queryParam("PageIndex", page)
                .queryParam("PageSize", 20)
                .build().toUriString();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);
        return responseEntity.getBody();
    }

    public static List<String> getCityCodes() throws IOException {
        Connection connect = Jsoup.connect("https://www.nhathuocankhang.com/he-thong-nha-thuoc");
        Document document = connect.get();
        Elements cityElements = document.select("ul[data-id][class='list-local'] > li[data-id]");
        log.info("City data: {}", cityElements.toArray());
        return cityElements.stream().map(cityElement -> cityElement.attr("data-id")).toList();
    }
}
