package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class NhatNhatCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://nhatnhat.com/cua-hang.html").get();
        List<String> categories = new java.util.ArrayList<>(document.select("#ddlProduct > option")
                .stream().map(Element::val).toList());
        categories.remove(0);
        categories.forEach(category -> {
            int page = tracePage(category, "nhatnhat_places");
            for (;;page ++) {
                String url = UriComponentsBuilder.fromHttpUrl("https://api.nhatnhat.com/api/nhathuoc/getwithquery")
                        .queryParam("sanpham", category)
                        .queryParam("page", page)
                        .toUriString();
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("token", "123456")
                        .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .build();
                try(Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                    String body = response.body().string();
                    JsonObject data = gson.fromJson(body, JsonObject.class).getAsJsonObject("data");
                    int finalPage = page;
                    data.getAsJsonArray("data")
                            .asList().stream().map(JsonElement::getAsJsonObject)
                            .forEach(store -> {
                                try {
                                    String id = store.get("id").getAsString();
                                    String name = store.get("name").getAsString();
                                    String address = store.get("diachi").getAsString();
                                    String googleLink = Jsoup.parse(store.get("map").getAsString()).selectFirst("iframe[src]").attr("src");
                                    saveDataWithHref(address, name, googleLink, id, category, finalPage, "nhatnhat_places");
                                } catch (Exception e) {
                                    log.error("Error while handle data: {}", store);
                                }
                            });
                    if(page >= data.get("last_page").getAsInt()) return;
                } catch (Exception e) {
                    log.error("Error: {}", e.getMessage());
                } finally {
                    int delta = new Random().nextInt(500);
                    Utils.sleep(1000 + delta);
                }
            }

        });
    }
}
