package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.DateTimeUtils;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class VntripCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        provinceNames().forEach(province -> {
            for (int page = 1; ; page++) {
                log.info("Handling page - province: {} - {}", page, province);
                String body;
                String url = UriComponentsBuilder.fromHttpUrl("https://micro-services.vntrip.vn/search-engine/search/vntrip-hotel-availability/")
                        .queryParam("seo_code", province)
                        .queryParam("check_in_date", DateTimeUtils.convertTodayToString("yyyyMMdd"))
                        .queryParam("nights", 1).queryParam("page_size", 100)
                        .queryParam("page", page).queryParam("request_source", "web_frontend")
                        .toUriString();
                while (true) {
                    try {
                        ResponseEntity<String> response = BaseHttpClient.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>("", BaseHttpClient.headers), String.class);
                        body = response.getBody();
                        break;
                    } catch (Exception e) {
                        log.error("Retry call api {}: {}", url, e.getMessage(), e);
                        Utils.sleep(2000);
                    }
                }

                JsonObject bodyObj = gson.fromJson(body, JsonObject.class);
                long total = bodyObj.get("total").getAsLong();
                if (total > 1600) {
                    log.warn("Skip total: {}", total);
                    return;
                } else {
                    log.info("Total: {}", total);
                }

                if (bodyObj.getAsJsonArray("data").isEmpty()) return;
                int finalPage = page;
                bodyObj.getAsJsonArray("data")
                        .asList().stream().map(JsonElement::getAsJsonObject)
                        .forEach(place -> {
                            try {
                                String id = place.get("vntrip_id").getAsString();
                                String name = place.get("name").getAsString();
                                String address = place.get("full_address").getAsString();
                                double lat = place.getAsJsonObject("location").get("lat").getAsDouble();
                                double lon = place.getAsJsonObject("location").get("lon").getAsDouble();
                                BaseEntity entity = BaseEntity.builder()
                                        .id(id).name(name).address(address).lon(lon).lat(lat)
                                        .placeCode(province).page(finalPage).build();
                                log.info("Place: {}", entity);
                                mongoTemplate.save(entity, "vntrip_places");
                            } catch (Exception e) {
                                log.error("Error while handle place: {}", e.getMessage(), e);
                            }

                        });
                Utils.sleepRandom(1000);
            }
        });
    }

    public static List<String> provinceNames() {
        return List.of("an-giang", "ba-ria-vung-tau", "bac-lieu", "bac-kan", "bac-giang", "bac-ninh", "ben-tre", "binh-dinh", "binh-duong", "binh-phuoc", "binh-thuan", "ca-mau", "can-tho", "cao-bang", "dak-lak", "dak-nong", "dien-bien", "dong-nai", "dong-thap", "gia-lai", "ha-giang", "ha-nam", "ha-tinh", "hai-duong", "hai-phong", "ha-noi", "hau-giang", "hoa-binh", "hung-yen", "khanh-hoa", "kien-giang", "kon-tum", "lai-chau", "lam-dong", "lang-son", "lao-cai", "long-an", "nam-dinh", "nghe-an", "ninh-binh", "ninh-thuan", "phu-tho", "phu-yen", "quang-binh", "quang-nam", "quang-ngai", "quang-ninh", "quang-tri", "soc-trang", "son-la", "tay-ninh", "thai-binh", "thai-nguyen", "thanh-hoa", "thua-thien-hue", "tien-giang", "tra-vinh", "tuyen-quang", "vinh-long", "vinh-phuc", "yen-bai", "da-nang", "sai-gon-ho-chi-minh");
    }
}
