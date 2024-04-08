package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.DateTimeUtils;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class ChuDuCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://khachsan.chudu24.com/").get();
        document.select(".hotel-cities a[href][class]")
                .stream().map(region -> region.absUrl("href"))
                .forEach(regionUrl -> {
                    for (int page = 1; ; page++) {
                        log.info("Handling page - url: {} - {}", page, regionUrl);
                        try {
                            String url = UriComponentsBuilder.fromHttpUrl(regionUrl).queryParam("page", page).toUriString();
                            Document regionDocument = Jsoup.connect(url).get();
                            if(!regionDocument.select(".paging-bottom-center li.page").isEmpty() && regionDocument.select(".paging-bottom-center li.page.active").isEmpty()) return;
                            int finalPage = page;
                            regionDocument.select("div.div-like-check")
                                    .stream().map(element -> element.attr("idint"))
                                    .forEach(idInt -> {
                                        try {
                                            String placeUrl = UriComponentsBuilder.fromHttpUrl("https://khachsan.chudu24.com/napi/ajax/hotel-search-room-price.njsx")
                                                    .queryParam("hotelIdInt", idInt)
                                                    .queryParam("checkIn", DateTimeUtils.convertTodayToString("DD/MM/YYYY"))
                                                    .queryParam("checkOut", DateTimeUtils.convertDayAfterToString(1, "DD/MM/YYYY"))
                                                    .queryParam("adult", 2).queryParam("children", 0)
                                                    .queryParam("numberOfRoom", 1).toUriString();
                                            ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(placeUrl, HttpMethod.POST, new HttpEntity<>("", BaseHttpClient.headers), String.class);
                                            String body = response.getBody();
                                            JsonObject hotelInfo = gson.fromJson(body, JsonObject.class).getAsJsonObject("data").getAsJsonObject("hotelInfo");
                                            String name = hotelInfo.get("hotelname").getAsString();
                                            String address = hotelInfo.get("address1").getAsString();
                                            String city = hotelInfo.get("cityname").getAsString();
                                            if(!address.contains(city)) {
                                                address = address + ", " + city;
                                            }
                                            double lat = hotelInfo.get("pointerlatitude").getAsDouble();
                                            double lon = hotelInfo.get("pointerlongtitude").getAsDouble();
                                            BaseEntity entity = BaseEntity.builder()
                                                    .id(idInt).name(name).address(address).lat(lat).lon(lon).placeCode(regionUrl).page(finalPage).build();
                                            log.info("Place: {}", entity);
                                            mongoTemplate.save(entity, "chu_du_places");
                                        } catch (Exception e) {
                                            log.error("Error while handle hotel id {}: {}", idInt, e.getMessage(), e);
                                        } finally {
                                            Utils.sleepRandom(100);
                                        }
                                    });
                            if(regionDocument.select(".paging-bottom-center li.page").isEmpty()) return;
                        } catch (Exception e) {
                            log.error("Error while handle request: {} {}", regionUrl, e.getMessage(), e);
                        } finally {
                            Utils.sleepRandom(1000);
                        }
                    }
                });
    }
}
