package hieu.dev.chapter9_webCrawler.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.dto.LongChauPharmacyItemResponse;
import hieu.dev.chapter9_webCrawler.dto.LongChauPharmacyRequest;
import hieu.dev.chapter9_webCrawler.entity.LongChauPharmacyEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

@Slf4j
public class LongChauPharmacyHttpClient extends BaseHttpClient {
    public static String getLocationsByProvince(String province) {
        String url = "https://api.nhathuoclongchau.com.vn/lccus/ecom-prod/store-front/v2/order-promissing/location-slug/list-shop";
        LongChauPharmacyRequest request = new LongChauPharmacyRequest(province);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(request), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return responseEntity.getBody();
    }

    public static List<String> getInitialProvinces() {
        String url = "https://nhathuoclongchau.com.vn/he-thong-cua-hang/ha-noi";
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String body = responseEntity.getBody();
        if(Objects.isNull(body)) {
            throw new RuntimeException("Get response body is null. " + url);
        }

        Document document = Jsoup.parse(body);
        String initialData = document.getElementById("__NEXT_DATA__").html();
        JsonObject initialObj = gson.fromJson(initialData, JsonObject.class);
        List<JsonElement> provinceObjList = initialObj.getAsJsonObject("props").getAsJsonObject("pageProps").getAsJsonArray("initialProvinces").asList();
        return provinceObjList.stream().map(provinceObj -> provinceObj.getAsJsonObject().get("slug").getAsString()).toList();
    }

}
