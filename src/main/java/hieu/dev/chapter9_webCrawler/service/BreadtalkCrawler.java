package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BreadtalkCrawler extends BaseSeleniumCrawler {
    public void crawlData() throws IOException {
        dropCollection("bread_talk_places");

        Document document = Jsoup.connect("https://breadtalkvietnam.com/he-thong-cua-hang/").get();
        List<String> provinceIds = document.select("#location-options-dropdown li").stream()
                .map(provinceElement -> provinceElement.attr("data-src"))
                .toList();
        provinceIds.forEach(provinceId -> {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
            RequestBody body = RequestBody.create(mediaType, String.format("action=location_list&id=%s", provinceId));
            Request request = new Request.Builder()
                    .url("https://breadtalkvietnam.com/wp-admin/admin-ajax.php")
                    .method("POST", body)
                    .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .addHeader("x-requested-with", "XMLHttpRequest")
                    .addHeader("Cookie", String.format("location-options=%s",provinceId))
                    .build();
            System.out.println(request);
            try(Response response = BaseHttpClient.okHttpClient.newCall(request).execute()) {
                String responseResponse = response.body().string();
                Document responseDocument = Jsoup.parse(responseResponse);
                responseDocument.select("li").forEach(store -> {
                    String googleLink = store.attr("data-embed");
                    String name = store.getElementsByTag("h6").first().text();
                    String address = store.getElementsByTag("address").first().text();
                    saveDataWithHref(address, name, googleLink, "bread_talk_places");
                });
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
        countDocuments("bread_talk_places");
    }
}
