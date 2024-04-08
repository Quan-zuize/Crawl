package hieu.dev.chapter9_webCrawler.client;

import hieu.dev.chapter9_webCrawler.dto.GovEncodeResponse;
import hieu.dev.chapter9_webCrawler.dto.GovReverseResponse;
import hieu.dev.chapter9_webCrawler.model.MapBoundary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GovHttpClient extends BaseHttpClient {
    public static void main(String[] args) throws Exception {
        Double distanceUnit = getDistanceUnit();
        MapBoundary map = MapBoundary.builder()
                .lat1(21.14343050391292).lon1(105.63903808593751)
                .currentLat(21.14343050391292).currentLon(105.63903808593751)
                .lat2(20.838277806058933).lon2(106.02081298828126).build();
        System.out.println(getMapBoundary("lat=21.14343050391292&lon=105.63903808593751", "lat=20.838277806058933&lon=106.02081298828126"));
        System.out.println(calculateTimeDays(map, distanceUnit, 1));

        testApi();
    }

    public static void testApi() throws Exception {
        GovReverseResponse vnPostResponse = callApiVnPost(21.45648363923508, 104.99957263469697);

        GovReverseResponse reverseResponse = callApiReverseAndDecrypt(21.45648363923508, 104.99957263469697);
        log.info("Place: {}", gson.toJson(reverseResponse.getPlaces()));

        GovEncodeResponse encodeResponse = callApi(21.01737421551212, 105.8061182498932);
        log.info("Addresses: {}", gson.toJson(encodeResponse.getResult().getAddressCompnent()));
    }

    public static GovReverseResponse callApiVnPost(double lat, double lon) {
        URI locationUri = UriComponentsBuilder.fromHttpUrl("https://maps.vnpost.vn/api/reverse")
                .queryParam("api-version", "1.2")
                .queryParam("apikey", "c7a3de12270dab17a4391add1d3c5d3d48a54d5b2104ec4f")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Dart/3.1 (dart:io)");
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> locationEntity = restTemplate.exchange(locationUri, HttpMethod.GET, entity, String.class);
        String data = locationEntity.getBody();
        GovReverseResponse govReverseResponse = gson.fromJson(data, GovReverseResponse.class);
        log.info("Response data: {}", gson.toJson(govReverseResponse));
        return govReverseResponse;
    }


    public static GovReverseResponse callApiReverseAndDecrypt(double lat, double lon) throws Exception {
        String reverseUri = UriComponentsBuilder.fromHttpUrl("https://api.diachiso.gov.vn/vpost/geocoding/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .build(true).toUriString();

        GovReverseResponse govReverseResponse = callApiAndDecrypt(reverseUri, headers, GovReverseResponse.class);
        log.info("Response data: {}", gson.toJson(govReverseResponse));
        return govReverseResponse;
    }

    public static GovEncodeResponse callApi(double lat, double lon) {
        URI encodeUri = UriComponentsBuilder.fromHttpUrl("https://maps.vnpost.vn/vpostcode/api/encode")
                .queryParam("location", lon + "," + lat)
                .build(true).toUri();

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> forEntity = restTemplate.exchange(encodeUri, HttpMethod.GET, entity, String.class);
        String data = forEntity.getBody();
        log.info("Response data: {}", data);
        return gson.fromJson(data, GovEncodeResponse.class);
    }

    public static List<Double> getMapBoundary(String... urls) {
        List<Double> boundary = new ArrayList<>();
        for (String url : urls) {
            String query = url.split("\\?").length == 2 ? url.split("\\?")[1] : url;
            String[] params = query.split("&");
            boundary.add(Double.valueOf(params[0].replace("lat=", "").replace("lon=", "")));
            boundary.add(Double.valueOf(params[1].replace("lat=", "").replace("lon=", "")));
        }
        return boundary;
    }

    private static Double getDistanceUnit() {
        return Math.sqrt(Math.pow(21.456429394922772 - 21.45648363923508, 2) + Math.pow(105.00134832057034 - 104.99957263469697, 2));
    }

    public static Long calculateTime(MapBoundary govMapBoundary, Double distanceUnit, int requestsPerSecond) {
        double searchedLatMapArea = Math.abs((govMapBoundary.getLon1() - govMapBoundary.getLon2()) * (govMapBoundary.getLat1() - govMapBoundary.getCurrentLat() - distanceUnit));
        double searchedLonMapArea = Math.abs(govMapBoundary.getLon1() - govMapBoundary.getCurrentLon()) * distanceUnit;
        double searchedMapArea = searchedLatMapArea + searchedLonMapArea;

        double mapArea = Math.abs(govMapBoundary.getLon1() - govMapBoundary.getLon2()) * (govMapBoundary.getLat1() - govMapBoundary.getLat2());
        double unitArea = Math.pow(distanceUnit, 2);
        return (long) ((mapArea - searchedMapArea) / unitArea * requestsPerSecond);
    }

    public static Double calculateTimeDays(MapBoundary govMapBoundary, Double distanceUnit, int requestsPerSecond) {
        return calculateTime(govMapBoundary, distanceUnit, requestsPerSecond) / 86400.0;
    }

    public static Double calculateTimeHours(MapBoundary govMapBoundary, Double distanceUnit, int requestsPerSecond) {
        return calculateTime(govMapBoundary, distanceUnit, requestsPerSecond) / 3600.0;
    }
}
