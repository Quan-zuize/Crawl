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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class THMilkCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        List<String> provinceIds = getProvinceIds();
        provinceIds.forEach(provinceId -> {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.thmilk.vn/wp-json/wp/v2/wpsl_stores")
                    .queryParam("wpsl_store_category", provinceId)
                    .queryParam("per_page", 100)
                    .queryParam("csrt", "10133272070307529834").toUriString();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
            gson.fromJson(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody()), JsonArray.class).asList()
                    .stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        System.out.println(store.toString());
                        String name = store.getAsJsonObject("title").get("rendered").getAsString();
                        String address = store.getAsJsonObject("meta").getAsJsonArray("wpsl_address").get(0).getAsString();
                        String latStr = store.getAsJsonObject("meta").getAsJsonArray("wpsl_lat").get(0).getAsString();
                        latStr = latStr.split(",")[0];

                        double lat = Double.parseDouble(latStr);
                        double lon = Double.parseDouble(store.getAsJsonObject("meta").getAsJsonArray("wpsl_lng").get(0).getAsString());
                        BaseEntity entity = BaseEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "th_milk_places");
                    });
        });
    }
    private List<String> getProvinceIds() {
        String url = "https://www.thmilk.vn/wp-json/wp/v2/wpsl_store_category?parent=2128&per_page=100&csrt=10133272070307529834\n";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", headers), String.class);
        return gson.fromJson(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody()), JsonArray.class)
                .asList().stream().map(JsonElement::getAsJsonObject)
                .map(provinceElement -> provinceElement.get("id").getAsString()).toList();
    }
}
