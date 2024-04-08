package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Service
@Slf4j
public class FptShopCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        for(int page = 1; ;page++) {
            String url = UriComponentsBuilder.fromHttpUrl("https://fptshop.com.vn/cua-hang/Home/GetListShop")
                    .queryParam("recordPerpage", 100)
                    .queryParam("page", page)
                    .toUriString();
            ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
            int totalrecordrest = jsonObject.get("totalrecordrest").getAsInt();
            if(totalrecordrest <= 0) return;
            String html = jsonObject.get("view").getAsString();
            Document document = Jsoup.parse(html, url);
            Elements elements = document.select(".branch-item");
            elements.forEach(element -> {
                String storeUrl = element.select("input").first().absUrl("data-url");
                System.out.println("Store url: " + storeUrl);
                chromeDriver.get(storeUrl);
                String address = chromeDriver.findElement(By.cssSelector(".common-text")).getText();
                String googleUrl = chromeDriver.findElements(By.cssSelector("iframe")).stream().map(webElement -> webElement.getAttribute("src"))
                        .filter(googleLink -> googleLink.contains("https://www.google.com")).findAny().orElse(null);
                saveDataWithHref(address, null, googleUrl, "fpt_shop_places");
            });
        }

    }
}
