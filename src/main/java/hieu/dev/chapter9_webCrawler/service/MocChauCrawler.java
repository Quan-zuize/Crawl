package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MocChauCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String url = "https://www.mcmilk.com.vn/wp-admin/admin-ajax.php?action=store_search&autoload=1";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());

        gson.fromJson(body, JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("store").getAsString();
                    String address = store.get("address").getAsString();
                    double lat = store.get("lat").getAsDouble();
                    double lon = store.get("lng").getAsDouble();

                    BaseEntity baseEntity = BaseEntity.builder()
                            .name(name).address(address).lat(lat).lon(lon).build();
                    System.out.println(baseEntity);
                    mongoTemplate.save(baseEntity, "moc_chau_places");
                });
    }
}
