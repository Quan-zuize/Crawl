package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.dto.MilanoItemResponse;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Slf4j
@Service
public class MilanoCoffeeCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, "action=pfget_markers&act=search&ne=&sw=&ne2=&sw2=&dt%5B0%5D%5Bname%5D=field296725954161956900000&dt%5B0%5D%5Bvalue%5D=&dt%5B1%5D%5Bname%5D=pointfinder_google_search_coord&dt%5B1%5D%5Bvalue%5D=&dt%5B2%5D%5Bname%5D=pointfinder_google_search_coord_unit&dt%5B2%5D%5Bvalue%5D=Km&dt%5B3%5D%5Bname%5D=pointfinder_radius_search&dt%5B3%5D%5Bvalue%5D=&dt%5B4%5D%5Bname%5D=pointfinder_areatype&dt%5B4%5D%5Bvalue%5D=&dt%5B5%5D%5Bname%5D=jobskeyword&dt%5B5%5D%5Bvalue%5D=&dt%5B6%5D%5Bname%5D=field527110067894682300000&dt%5B6%5D%5Bvalue%5D=&dtx%5B0%5D%5Bname%5D=pointfinderltypes&dtx%5B0%5D%5Bvalue%5D=55&dtx%5B1%5D%5Bname%5D=pointfinderlocations&dtx%5B1%5D%5Bvalue%5D=&dtx%5B2%5D%5Bname%5D=pointfinderconditions&dtx%5B2%5D%5Bvalue%5D=&dtx%5B3%5D%5Bname%5D=pointfinderitypes&dtx%5B3%5D%5Bvalue%5D=&dtx%5B4%5D%5Bname%5D=pointfinderfeatures&dtx%5B4%5D%5Bvalue%5D=&ppp=-1&paged=1&order=&orderby=&cl=&security=18400d2dd2");
        Request request = new Request.Builder()
                .url("https://daily.milanocoffee.com.vn/wp-content/plugins/pointfindercoreelements/includes/pfajaxhandler.php")
                .method("POST", body)
                .addHeader("authority", "daily.milanocoffee.com.vn")
                .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("accept-language", "en-US,en;q=0.9,vi;q=0.8")
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("origin", "https://daily.milanocoffee.com.vn")
                .addHeader("referer", "https://daily.milanocoffee.com.vn/")
                .addHeader("sec-ch-ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Linux\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .build();
        while (true) {
            try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                JsonObject responseBodyObj = gson.fromJson(responseBody, JsonObject.class);
                JsonObject mapData = responseBodyObj.getAsJsonObject("data");
                if (mapData.isEmpty()) {
                    return;
                }
                Map<String, MilanoItemResponse> data = gson.fromJson(gson.toJson(mapData), new TypeToken<Map<String, MilanoItemResponse>>() {}.getType());
                data.values().stream().map(MilanoItemResponse::toEntity)
                        .forEach(milanoEntity -> {
                            System.out.println(milanoEntity);
                            mongoTemplate.save(milanoEntity, "milano_places");
                        });
                return;
            } catch (Exception e) {
                log.error("Error {}", e.getMessage());
                Utils.sleep(1000);
            }
        }

    }
}
