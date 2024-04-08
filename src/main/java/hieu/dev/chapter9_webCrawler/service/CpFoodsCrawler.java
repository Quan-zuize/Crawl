package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class CpFoodsCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(2, ChronoUnit.SECONDS));
        chromeDriver.get("https://cpfoods.vn/pages/he-thong-cua-hang");
        String urlMap = chromeDriver.executeScript("return urlMap").toString();
//        String urlMap = "https://file.hstatic.net/1000115147/file/data.json_4350bc77121a4af2a2b92d9c9a48e8ab.txt";

        Request request = new Request.Builder().url(urlMap)
                .get()
                .headers(Headers.of(BaseHttpClient.headers.toSingleValueMap()))
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()){
            List<JsonElement> storeList = gson.fromJson(response.body().string(), JsonObject.class).getAsJsonArray("cuahang").asList();
            System.out.println(storeList.size());
            storeList.stream().map(JsonElement::getAsJsonObject)
                    .forEach(store -> {
                        String name = store.get("shop_name").getAsString();
                        String address = store.get("address").getAsString();
                        String googleLink = Jsoup.parse(store.get("map").getAsString()).getElementsByTag("iframe").first().attr("src");
                        saveDataWithHref(address, name, googleLink, "cp_foods_places");
//                        saveData(address, address, name, "cp_foods_places");
                    });
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
        }

    }
}
