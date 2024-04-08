package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;

@Service
public class NutiFoodCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        for (int page = 1; ; page++) {
            String url = "https://api.dgvdigital.net/third-party/api/v1/nuti-food/search-merchant-store";
            JsonObject requestObj = new JsonObject();
            requestObj.add("pageIndex", new JsonPrimitive(page));
            requestObj.add("pageSize", new JsonPrimitive(10));
            requestObj.add("merchantId", new JsonPrimitive("63267346183ccd0c0c93d6e7"));
            HttpEntity<String> entity = new HttpEntity<>(gson.toJson(requestObj), headers);
            ResponseEntity<String> response;

            try {
                response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            } catch (HttpClientErrorException e) {
                return;
            }

            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
            JsonObject responseObj = gson.fromJson(body, JsonObject.class);
            if (!responseObj.get("isSuccess").getAsBoolean()) return;

            responseObj.getAsJsonObject("data").getAsJsonArray("data")
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        JsonObject locationElement = store.getAsJsonObject("location");
                        JsonElement latitudeElement = locationElement.get("latitude");
                        if (Objects.isNull(latitudeElement) || latitudeElement.isJsonNull() || latitudeElement.getAsDouble() == 0)
                            return;
                        String name = store.get("name").getAsString();
                        JsonElement addressElement = locationElement.get("fullAddress");
                        String address = getAddress(addressElement, locationElement, store);
                        double lat = locationElement.get("latitude").getAsDouble();
                        double lon = locationElement.get("longitude").getAsDouble();

                        BaseEntity baseEntity = BaseEntity.builder().address(address).name(name).lon(lon).lat(lat).build();
                        System.out.println(baseEntity);
                        mongoTemplate.save(baseEntity, "nuti_food_places");
                    });
            Utils.sleep(100);
        }

    }

    private static String getAddress(JsonElement addressElement, JsonObject locationElement, JsonObject store) {
        try {
            if (Objects.nonNull(addressElement) && !addressElement.isJsonNull()) return addressElement.getAsString();

            return locationElement.get("address").getAsString() + ", " + locationElement.get("wardName").getAsString() + ", " +
                    locationElement.get("districtName").getAsString() + ", " + locationElement.get("provinceName").getAsString();
        } catch (Exception e) {
            System.out.println("Error get address: " + store);
            return null;
        }

    }

}
