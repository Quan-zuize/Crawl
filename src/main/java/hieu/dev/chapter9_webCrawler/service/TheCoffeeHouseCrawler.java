package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.TheCoffeeHouseEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class TheCoffeeHouseCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;
//    @PostConstruct
    public void crawlTheCoffeeHouse() throws IOException {
        List<String> cityIds = getCityIds();
        for (String cityId : cityIds) {
            int page = 0;
            while (true) {
                try {
                    String url = UriComponentsBuilder.fromHttpUrl("https://thecoffeehouse.com/search")
                            .queryParam("q", String.format("filter=((blogid:article=%s))", cityId))
                            .queryParam("view", "get_stores")
                            .queryParam("page", page).build().toUriString();
                    String body = Jsoup.connect(url).get().toString();
                    body = body.replace("&lt;iframe", "<iframe").replace("&gt;&lt;/iframe&gt;", "></iframe>");

                    Document document = Jsoup.parse(body);
                    Elements divElements = document.select("div[data-total-pages]");
                    if(divElements.isEmpty()) return;

                    log.info("Handel data cityId-page-size: {}-{}-{}", cityId, page, divElements.size());
                    TheCoffeeHouseEntity entity = new TheCoffeeHouseEntity();
                    for (Element divElement : divElements) {
                        String name = divElement.getElementsByTag("p").first().text();
                        entity.setName(name);

                        Elements trElements = divElement.getElementsByTag("tr");
                        int trAddressIndex = -1;
                        for (int i = 0; i < trElements.size(); i++) {
                            if(trElements.get(i).getElementsByTag("td").first().text().equals("address")) {
                                trAddressIndex = i; break;
                            }
                        }
                        if(trAddressIndex != -1) {
                            String address = trElements.get(trAddressIndex).getElementsByTag("td").get(1).text();
                            entity.setAddress(address);
                        }

                        String href = divElement.selectFirst("iframe").absUrl("src");
                        List<Double> coordinates = Utils.handleCoordinatesByUrl(href);
                        if(!CollectionUtils.isEmpty(coordinates)) {
                            double lat = coordinates.get(0);
                            double lon = coordinates.get(1);
                            entity.setLat(lat);
                            entity.setLon(lon);
                            log.info("Place: {}", gson.toJson(entity));
                            mongoTemplate.save(entity);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while handle city-page: {}-{}", cityId, page);
                    break;
                } finally {
                    page++;
                    Utils.sleep(200);
                }
            }

        }

    }

    private static List<String> getCityIds() throws IOException {
        Connection connect = Jsoup.connect("https://thecoffeehouse.com/pages/danh-sach-tat-ca-cua-hang");
        Document document = connect.get();
        Element sideBar = document.selectFirst("aside[class='page_sidebar']");
        Elements cityIdElements = sideBar.select("a[href]");
        return cityIdElements.stream().map(cityIdElement -> cityIdElement.attr("href")).toList();
    }
}
