package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class GivralBakeryCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String url = UriComponentsBuilder.fromHttpUrl("https://givralbakery.com.vn/modules/dealer/ajax/maps.php")
                .queryParam("r", System.currentTimeMillis()).toUriString();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, "keyword=&catid=0&city=&state=&page=1&curID=&zoom=6&center=&lstPoint=&searchType=0&isPageLoad=true&sort=0&v=1710832699833&lang=vn");
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Cookie", "PHPSESSID=quqjn5h4ake35punpmm1f1suj4")
                .build();
        try(Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            gson.fromJson(responseBody, JsonObject.class)
                    .getAsJsonArray("data").asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String address = store.get("address").getAsString();
                        double lat = store.get("lat").getAsDouble();
                        double lon = store.get("lon").getAsDouble();
                        BaseEntity entity = BaseEntity.builder()
                                .address(address).lon(lon).lat(lat).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "givral_bakery_places");
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        countDocuments("givral_bakery_places");
    }
}
