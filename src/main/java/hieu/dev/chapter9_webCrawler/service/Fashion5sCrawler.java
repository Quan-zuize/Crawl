package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v118.network.Network;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Service;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class Fashion5sCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://5sfashion.vn/he-thong-cua-hang").get();
        document.getElementsByClass("place--wrap__content")
                .forEach(store -> {
                    String name = store.getElementsByClass("title").text();
                    String address = store.getElementsByClass("address").text();
                    String google = address.split("\\(")[0];
                    saveData(google, address, name, "fashion5s_places");
                });
    }

    public static void main(String[] args) {

//    }
//    public void crawl2() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        Actions actions = new Actions(chromeDriver);

        DevTools devTools = devTools(chromeDriver);
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            String url = responseReceived.getResponse().getUrl();
            if(!URLDecoder.decode(url).contains("https://5sfashion.vn/livewire/message/showroom.web.showroom-page")) return;
            String body = devTools.send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
            JsonObject responseObj = gson.fromJson(body, JsonObject.class);
            String html = responseObj.getAsJsonObject("effects").get("html").getAsString();
            String googleHtml = responseObj.getAsJsonObject("serverMemo").getAsJsonObject("data").get("map").getAsString();
            String googleLink = Jsoup.parse(HtmlEscape.unescapeHtml(googleHtml)).getElementsByTag("iframe").first().attr("src");
            System.out.println("google link: " + googleLink);
        });

        chromeDriver.get("https://5sfashion.vn/he-thong-cua-hang");
        chromeDriver.findElements(By.className("place--wrap__content"))
                .forEach(store -> {
                    String name = store.findElement(By.className("title")).getText();
                    String address = store.findElement(By.className("address")).getText();
                    try {
                        actions.moveToElement(store).click().perform();
                    } catch (Exception e){}
                });
    }
}
