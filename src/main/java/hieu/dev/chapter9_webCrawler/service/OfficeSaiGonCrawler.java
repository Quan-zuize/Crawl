package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Service
public class OfficeSaiGonCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        for (int page = 0; ; page++) {
            String pageUrl = UriComponentsBuilder.fromHttpUrl("https://www.officesaigon.vn/searchs/0/0/0/0/0/0/0/")
                    .queryParam("page", page).build().toUriString();
            Document document = Jsoup.connect(pageUrl).get();
            Elements elements = document.getElementsByClass("officelist").first().getElementsByTag("li");
            if(elements.isEmpty()) return;
            for (Element element : elements) {
                try {
                    String name = element.getElementsByTag("h3").text();
                    String elementHref = element.getElementsByTag("h3").first().getElementsByTag("a").attr("abs:href");
                    Document elementDocument = Jsoup.connect(elementHref).get();
                    String googleUrl = elementDocument.select("iframe[src][allowfullscreen][loading]").attr("abs:src");
                    String address = getAddress(element, elementDocument);
                    saveDataWithHref(address, name, googleUrl, "office_saigon_places");
                } catch (Exception e) {
                    System.out.println("ERROR page " + page + ": " + element.getElementsByTag("h3"));
                }

            }
        }
    }

    private String getAddress(Element element, Document elementDocument) {
        try {
            return elementDocument.getElementsByClass("map_address").first().getElementsByTag("p").get(1).text();
        } catch (Exception e) {
            return element.getElementsByTag("p").get(1).text();
        }
    }
}
