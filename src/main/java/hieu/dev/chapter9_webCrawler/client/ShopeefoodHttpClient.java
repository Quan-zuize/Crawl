package hieu.dev.chapter9_webCrawler.client;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.model.ShopeefoodCity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ShopeefoodHttpClient extends BaseHttpClient{
    static {
        headers.add("x-foody-access-token", "");
        headers.add("x-foody-api-version", "1");
        headers.add("x-foody-app-type", "1004");
        headers.add("x-foody-client-id", "");
        headers.add("x-foody-client-language", "vi");
        headers.add("x-foody-client-type", "1");
        headers.add("x-foody-client-version", "3.0.0");
    }
    public static void main(String[] args) {
        getCities();
        getLocationData();
    }
    public static void getLocationData() {
        String url = "https://gappapi.deliverynow.vn/api/delivery/get_browsing_ids";

        HttpEntity<String> entity = new HttpEntity<>("{\"city_id\":218,\"sort_type\":30,\"foody_services\":[1],\"district_ids\":[22]}", headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
        log.info("Data: {}", body);
    }
    public static List<ShopeefoodCity> getCities() {
        try {
            String url = "https://gappapi.deliverynow.vn/api/meta/get_metadata";
            String metaDataStr = new String(Files.readAllBytes(Paths.get("src/main/java/hieu/dev/chapter9_webCrawler/client/ShopeefoodMetaData.json")));
            JsonObject metaData = gson.fromJson(metaDataStr, JsonObject.class);
            String citiesStr = metaData.getAsJsonObject("reply").getAsJsonObject("country").getAsJsonArray("cities").toString();

            List<ShopeefoodCity> cities = gson.fromJson(citiesStr, new TypeToken<List<ShopeefoodCity>>(){}.getType());
            long districtSize = cities.stream().mapToLong(shopeefoodCity -> shopeefoodCity.getDistricts().size()).sum();
            log.info("cities size: {}, district size: {}, {}", cities.size(), districtSize, gson.toJson(cities));
            return cities;
        } catch (Exception e) {
            log.error("Error while get list shopeefood cities: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
