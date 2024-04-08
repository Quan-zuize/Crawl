package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.util.Objects;

@Service
public class SavoureBakeryCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        for (int page = 1; ; page++) {
            String url = UriComponentsBuilder.fromHttpUrl("https://savourebakery.com/cua-hang.html/")
                    .queryParam("p", page).toUriString();

            HttpHeaders httpHeaders = new HttpHeaders(BaseHttpClient.headers);
            httpHeaders.set("accept", "text/html");
            ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", httpHeaders), String.class);
            Document document = Jsoup.parse(Objects.requireNonNull(HtmlEscape.unescapeHtml(response.getBody())));
            double currentPage = Double.parseDouble(document.getElementsByClass("pagecur").first().text());
            if (currentPage != page) break;

            document.select(".vnt-store .store")
                    .forEach(store -> {
                        try {
                            String name = store.selectFirst(".tend").text();
                            String address = store.selectFirst(".fa-home").ownText();
                            String googleDataLink = store.selectFirst(".link > a[data-src]").absUrl("data-src");

                            Document googleData = Jsoup.connect(googleDataLink).get();
                            String googleLink = googleData.selectFirst("iframe[src]").attr("src");
                            saveDataWithHref(address, name, googleLink, "savoure_bakery_places");
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                    });
        }
        countDocuments("savoure_bakery_places");
    }
}
