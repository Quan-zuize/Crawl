package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;

@Service
public class NEMCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://nemshop.vn/pages/he-thong-cua-hang").get();
        document.select("li[data-tinh] > a[href]").forEach(element -> {
            String address = element.ownText();

            if(address.split(":").length > 1) address = address.split(":")[1];
            String url = UriComponentsBuilder.fromHttpUrl(element.absUrl("href"))
                    .queryParam("view", "cuahang").toUriString();
            ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
            String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
            if(Strings.isEmpty(body)) return;
            Document googleMapDocument = Jsoup.parse(HtmlEscape.unescapeHtml(body));

            String googleUrl = null;
            if(!googleMapDocument.getElementsByTag("iframe").isEmpty()) {
                googleUrl = googleMapDocument.getElementsByTag("iframe").first().attr("src");
            }
            saveDataWithHref(address, null, googleUrl, "nem_places");
        });
    }
}
