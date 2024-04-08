package hieu.dev.chapter9_webCrawler.client;

import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.dto.OSMReverseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class OSMHttpClient extends BaseHttpClient{
    public static void main(String[] args) {
        System.out.println(lookupDataByIds(List.of("N7918459155")));
        System.out.println(reverseDataByLocation(108.789745, 15.1154991));
    }

    public static List<OSMReverseResponse> lookupDataByIds(List<String> osmIds) {
        URI lookupUri = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/lookup")
                .queryParam("osm_ids", String.join(",", osmIds))
                .queryParam("format", "json")
                .build(true).toUri();

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(lookupUri, HttpMethod.GET, entity, String.class);
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            String data = responseEntity.getBody();
            List<OSMReverseResponse> result = gson.fromJson(data, new TypeToken<List<OSMReverseResponse>>() {}.getType());
            log.info("OSM Nominatim Response size {}: {}", Objects.nonNull(result) ? result.size() : 0, data);
            return result;
        } else {
            log.error("Error while call api: {}", responseEntity.getStatusCode());
        }

        return new ArrayList<>();
    }

    public static OSMReverseResponse reverseDataByLocation(Double lon, Double lat) {
        URI reverseUri = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/reverse")
                .queryParam("lon", lon)
                .queryParam("lat", lat)
                .queryParam("format", "json")
                .build(true).toUri();

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(reverseUri, HttpMethod.GET, entity, String.class);
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            String data = responseEntity.getBody();
            log.info("OSM Nominatim Response: {}", data);
            return gson.fromJson(data, OSMReverseResponse.class);
        } else {
            log.error("Error while call api: {}", responseEntity.getStatusCode());
        }

        return null;
    }
}
