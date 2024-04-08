package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.CertUtils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;

@Service
@Slf4j
public class OfficeHaNoiCrawler extends BaseHttpClient {
    public static final HttpHeaders headers = new HttpHeaders();

    static {
        CertUtils.ignoreCertificates();
        headers.add("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36");
        headers.add("accept", "application/json, text/javascript, */*; q=0.01");
        headers.add("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.add("x-requested-with", "XMLHttpRequest");
    }

    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        for (int page = 0; ; page++) {
            String requestBody = String.format("action=search_results_custom&filter_city=ha-noi&filter_district=&filter_category=&filter_price=&filter_acreage=&language=vi&paged=%d&type=ajax", page);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange("https://officespace.vn/wp-admin/admin-ajax.php", HttpMethod.POST, entity, String.class);
            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
            String html = gson.fromJson(body, JsonObject.class).get("data_html").getAsString();
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByClass("office-item");
            if (elements.isEmpty()) return;
            for (Element element : elements) {
                try {
                    String id = element.attr("data-id");
                    String name = element.getElementsByTag("h3").text();
                    String address = element.getElementsByClass("address-building").text();
                    String elementHref = element.getElementsByTag("h3").first().getElementsByTag("a").attr("abs:href");
                    Document elementDocument = Jsoup.connect(elementHref).get();
                    Element latLonElement = elementDocument.getElementById("google_map");
                    double lat = Double.parseDouble(latLonElement.attr("data-lat"));
                    double lon = Double.parseDouble(latLonElement.attr("data-long"));

                    BaseEntity baseEntity = BaseEntity.builder()
                            .id(id).name(name).address(address).lat(lat).lon(lon).build();
                    mongoTemplate.save(baseEntity, "office_space_places");
                    log.info("Place: {}", baseEntity);
                } catch (Exception e) {
                    log.error("ERROR page {}: {}", page, element, e);
                }

            }
        }
    }

}
