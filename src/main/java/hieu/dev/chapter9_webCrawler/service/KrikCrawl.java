package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
public class KrikCrawl extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://krik.vn/he-thong-cua-hang").get();
        String data = document.getElementById("locations").html();
        gson.fromJson(data, JsonArray.class).asList()
                .stream().map(JsonElement::getAsJsonObject)
                .forEach(store -> {
                    String name = store.get("name").getAsString();
                    String address = store.get("address").getAsString();

                    if (Objects.isNull(store.get("content")) || store.get("content").isJsonNull() || store.get("content").getAsString().isEmpty()) {
                        saveData(address, name, "krik_places");
                    } else {
                        String googleLink = Jsoup.parse(HtmlEscape.unescapeHtml(store.get("content").getAsString()))
                                .getElementsByTag("iframe").first().attr("src");
                        saveDataWithHref(address, name, googleLink, "krik_places");
                    }
                });
    }
}
