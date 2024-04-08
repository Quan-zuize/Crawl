package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Service
public class FreshGardenCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://freshgarden.vn/pages/danh-sach-cua-hang").get();
        document.select("div.map_list div[data-href]")
                .forEach(store -> {
                    String name = store.text();
                    String dataLink = store.absUrl("data-href");
                    String url = UriComponentsBuilder.fromHttpUrl(dataLink)
                            .queryParam("view", "cuahang").toUriString();
                    ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
                    String body = response.getBody();
                    Document dataDocument = Jsoup.parse(body);
                    if(dataDocument.select("iframe[src]").isEmpty()) return;

                    String address = dataDocument.text();
                    address = address.split(":")[1].replace("Số điện thoại", "");
                    String googleLink = dataDocument.selectFirst("iframe[src]").attr("src");
                    saveDataWithHref(address, name, googleLink, "fresh_garden_places");
                });
        countDocuments("fresh_garden_places");
    }
}
