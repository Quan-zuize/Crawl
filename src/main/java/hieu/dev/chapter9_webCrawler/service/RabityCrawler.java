package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class RabityCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://rabity.vn/pages/danh-sach-cua-hang/");
        String url = chromeDriver.executeScript("return url").toString();

        ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
        String body = response.getBody();
        String data = body.replaceAll("\\);$", "").replace("google.visualization.Query.setResponse(", "");

        gson.fromJson(data, JsonObject.class)
                .get("table").getAsJsonObject()
                .getAsJsonArray("rows")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .map(store -> store.getAsJsonArray("c"))
                .forEach(store -> {
                    if (Objects.isNull(store)) return;
                    if (store.get(0).getAsJsonObject().get("v").isJsonNull()) return;
                    String name = store.get(1).getAsJsonObject().get("v").getAsString();
                    String address = store.get(2).getAsJsonObject().get("v").getAsString();
                    address = address.split("\n")[0];
                    String googleLink = Jsoup.parse(store.get(3).getAsJsonObject().get("v").getAsString()).getElementsByTag("iframe").attr("src");
                    saveDataWithHref(address, name, googleLink, "rabity_places");
                });

    }
}
