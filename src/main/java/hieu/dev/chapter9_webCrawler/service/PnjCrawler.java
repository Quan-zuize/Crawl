package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
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
import java.util.Map;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class PnjCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        getProvinceIds().forEach(provinceId -> {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, String.format("state_code=%s&district_code=0", provinceId));
            Request request = new Request.Builder()
                    .url("https://www.pnj.com.vn/index.php?dispatch=mp_stores.search_stores")
                    .method("POST", body)
                    .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .addHeader("x-requested-with", "XMLHttpRequest")
                    .addHeader("Cookie", "sid_customer_1f31a=acab9e2bb6ec5319e6cee05c821caeb4-C; mp_version_change=4.3.4.2044")
                    .build();
            try(Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                String html = response.body().string();
                String data = Jsoup.parse(html).selectFirst("input[type='hidden']").val();
                Map<String, JsonObject> dataMap = gson.fromJson(data, new TypeToken<Map<String, JsonObject>>(){}.getType());
                dataMap.values().forEach(store -> {
                    String name = store.get("store_name").getAsString();
                    String address = store.get("address").getAsString();
                    String googleLink = store.get("store_address_link_map").getAsString();
                    saveDataWithHref(address, name, googleLink, "pnj_places");
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                Utils.sleep(200);
            }
        });
    }

    private static List<String> getProvinceIds() throws IOException {
        Document document = Jsoup.connect("https://www.pnj.com.vn/ho-tro-mua-hang/he-thong-cua-hang/").get();
        List<String> provinceIds = new java.util.ArrayList<>(document.select("#elm_stores_state > option")
                .stream().map(Element::val).toList());
        provinceIds.remove(0);
        return provinceIds;
    }
}
