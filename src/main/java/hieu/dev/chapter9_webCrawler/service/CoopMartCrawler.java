package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CoopMartCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        httpHeaders.set("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> entity = new HttpEntity<>("{}", httpHeaders);

        String groupUrl = "http://www.co-opmart.com.vn/DataserviceProvider.asmx/GetBranch?cityId=0&districtId=0&groupId=0&s=";
        ResponseEntity<String> response = restTemplate.exchange(groupUrl, HttpMethod.POST, entity, String.class);
        getDData(org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody()))
                .stream().map(JsonElement::getAsJsonObject).map(group -> group.get("ID").getAsString())
                .forEach(groupId -> {
                    String storeListUrl = String.format("http://www.co-opmart.com.vn/DataserviceProvider.asmx/GetBranch?cityId=0&districtId=0&groupId=%s&s=", groupId);
                    ResponseEntity<String> storesResponse = restTemplate.exchange(storeListUrl, HttpMethod.POST, entity, String.class);
                    getDData(org.unbescape.html.HtmlEscape.unescapeHtml(storesResponse.getBody())).forEach(store -> {
                        try {
                            String name = store.get("Name").getAsString().trim();
                            String address = store.get("Address").getAsString().trim();
                            double lat = store.get("Latitude").getAsDouble();
                            double lon = store.get("Longitude").getAsDouble();
                            BaseEntity baseEntity = BaseEntity.builder()
                                    .name(name).address(address).lon(lon).lat(lat).build();
                            System.out.println(baseEntity);
                            mongoTemplate.save(baseEntity, "coop_mart_places");
                        } catch (Exception e) {
                            log.error("Error: {}", e.getMessage(), e);
                        }
                    });
                    Utils.sleep(200);
                });
    }
    private static List<JsonObject> getDData(String body) {
        String dStr = gson.fromJson(body, JsonObject.class).get("d").getAsString();
        return gson.fromJson(dStr, JsonArray.class).asList().stream().map(JsonElement::getAsJsonObject).toList();
    }
}
