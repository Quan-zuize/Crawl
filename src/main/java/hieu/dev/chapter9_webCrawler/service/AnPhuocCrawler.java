package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.unbescape.html.HtmlEscape;

@Service
public class AnPhuocCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String url = "https://www.anphuoc.com.vn/DealerLocator/Services/GetDealerXml.ashx?distance=NaN&lat=11&lng=107&languageid=-1&siteid=1&zoneguid=1f3413ce-87bd-42be-8301-168477b03679&country=99f791e7-7343-42e8-8c19-3c41068b5f8d&province=&district=";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = HtmlEscape.unescapeHtml(response.getBody());
        assert body != null;
        Document document = Jsoup.parse(body);
        document.getElementsByTag("marker").forEach(store -> {
            String address = store.attr("address");
            String name = store.attr("name");
            double lat = Double.parseDouble(store.attr("lat"));
            double lon = Double.parseDouble(store.attr("lng"));

            BaseEntity entity = BaseEntity.builder().address(address).name(name).lon(lon).lat(lat).build();
            System.out.println(entity);
            mongoTemplate.save(entity, "an_phuoc_places");
        });
    }
}
