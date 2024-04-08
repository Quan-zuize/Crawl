package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.unbescape.html.HtmlEscape;

import java.util.Objects;

@Service
public class GuardianCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        for (int page = 1; ; page++) {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.guardian.com.vn/amlocator/index/ajax/")
                    .queryParam("p", page).toUriString();
            HttpHeaders httpHeaders = new HttpHeaders(headers);
            httpHeaders.set("x-requested-with", "XMLHttpRequest");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>("", headers), String.class);
            String body = response.getBody();
            JsonArray items = gson.fromJson(body, JsonObject.class).getAsJsonArray("items");
            if (items.get(0).getAsJsonObject().get("id").getAsLong() == 1 && page != 1) return;
            items.asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        if(Objects.isNull(store.get("lat")) || store.get("lat").isJsonNull()) return;
                        double lat = store.get("lat").getAsDouble();
                        double lon = store.get("lng").getAsDouble();
                        String html = HtmlEscape.unescapeHtml(store.get("popup_html").getAsString());
                        Document document = Jsoup.parse(html);

                        String name = document.getElementsByTag("a").first().text();
                        String address = document.body().text().split("Address:")[1].split("Description:")[0].trim();

                        BaseEntity entity = BaseEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "guardian_places");
                    });
        }

    }
}
