package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.compress.GZIP;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BachHoaXanhCrawler extends BaseHttpClient {
    @Autowired
    public MongoTemplate mongoTemplate;
    private static final HttpHeaders httpHeaders = new HttpHeaders(headers);

    static {
        httpHeaders.set("authorization", "Bearer 76945FCB5CDBB6555488F3884AA3706E");
        httpHeaders.set("deviceid", "bbddd6f2-2d9d-417a-8e54-15b518275fdf");
        httpHeaders.set("reversehost", "http://bhxapi.live");
        httpHeaders.set("content-type", "application/json");
        httpHeaders.set("connection", "keep-alive");
    }

    public void crawl() {
        List<String> provinceList = getProviceList();
        provinceList.forEach(provinceId -> {
            getStoreLocations(provinceId).stream().map(JsonElement::getAsJsonObject).forEach(storeLocation -> {
                String id = storeLocation.get("storeId").getAsString();
                String name = storeLocation.get("storeName").getAsString();
                String address = storeLocation.get("displayStoreAddress").getAsString();
                address = address.split("\\(")[0];
                double lat = storeLocation.get("lat").getAsDouble();
                double lon = storeLocation.get("lng").getAsDouble();
                BaseEntity entity = BaseEntity.builder()
                        .id(id).lat(lat).lon(lon).name(name).address(address).build();
                System.out.println(entity);
                mongoTemplate.save(entity, "bach_hoa_xanh_places");
            });
        });
    }

    public static List<String> getProviceList() {
        String url = "https://apibhx.tgdd.vn/Location/GetFullDataLocationV2";
        HttpEntity<String> entity = new HttpEntity<>("", httpHeaders);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        String responseBody = GZIP.gzipDecompress(response.getBody());

        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject.getAsJsonObject("data")
                .getAsJsonArray("provinceList").asList()
                .stream().map(JsonElement::getAsJsonObject).map(obj -> obj.getAsJsonPrimitive("id").getAsString()).toList();
    }

    public static List<JsonElement> getStoreLocations(String id) {
        JsonObject requestBody = new JsonObject();
        requestBody.add("provinceId", new JsonPrimitive(id));
        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(requestBody), httpHeaders);
        ResponseEntity<byte[]> response = restTemplate.exchange("https://apibhx.tgdd.vn/Location/GetStoreByLocation", HttpMethod.POST, entity, byte[].class);
        String responseBody = GZIP.gzipDecompress(response.getBody());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject.getAsJsonObject("data").getAsJsonArray("stores").asList();
    }
}
