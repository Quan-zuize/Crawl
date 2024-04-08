package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class JtexpressCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://jtexpress.vn/vi/tracking?type=office-point").get();
        List<String> provinces = new java.util.ArrayList<>(document.select("#dropdown-from-prov-tariff > option").stream().map(Element::val).toList());
        provinces.remove(0);
        provinces.forEach(province -> {
            try {
                String responseBody = getDistrictsResponse(province);
                JsonObject responseBodyObj = gson.fromJson(responseBody, JsonObject.class);
                String provinceHtml = responseBodyObj.get("search/ajax/district").getAsString();
                Document provinceDocument = Jsoup.parse(provinceHtml);
                List<String> districtLists = new java.util.ArrayList<>(provinceDocument.select("option")
                        .stream().map(Element::val).toList());
                districtLists.remove(0);
                districtLists.forEach(district -> {
                    String storesResponse = getStoresResponse(province, district);
                    String storesHtml = gson.fromJson(storesResponse, JsonObject.class).get("search/office/result-office").getAsString();
                    Document storeDocument = Jsoup.parse(storesHtml);
                    String name = storeDocument.selectFirst("span").text();
                    String address = storeDocument.selectFirst(".items-start").text();
                    String googleLink = storeDocument.selectFirst("a[href]").attr("href");
                    saveDataWithHref(address, name, googleLink, "jte_express_places");
                });
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }

        });
    }

    private static String getDistrictsResponse(String province) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, String.format("province_id=%s", province));
        Request request = new Request.Builder()
                .url("https://jtexpress.vn/vi/tracking?type=office-point")
                .method("POST", body)
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("x-october-request-handler", "onChangeProvince")
                .addHeader("x-october-request-partials", "search/ajax/district")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .build();
        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute();){
            return response.body().string();
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return null;
        }
    }

    private static String getStoresResponse(String province, String district) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, String.format("province_id=%s&district_id=%s", province, district));
        Request request = new Request.Builder()
                .url("https://jtexpress.vn/vi/tracking?type=office-point")
                .method("POST", body)
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("x-october-request-handler", "onSearchOffice")
                .addHeader("x-october-request-partials", "search/office/result-office")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .build();
        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute();){
            return response.body().string();
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return null;
        }
    }
}
