package hieu.dev.chapter9_webCrawler.selenium;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.GoogleLocationEntity;
import hieu.dev.chapter9_webCrawler.model.MapBoundary;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.remote.RemoteWebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.Utils.distance;
import static hieu.dev.chapter9_webCrawler.Utils.doRetry;
import static hieu.dev.chapter9_webCrawler.client.BaseHttpClient.gson;

@Service
@Slf4j
public class GoogleSeleniumCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    private static MapBoundary mapBoundary;
    public static final double DISTANCE_UNIT = 1.5 * distance(21.096376, 105.931481, 21.077356, 105.932125);

//    @PostConstruct
    public void crawlData() {
        long start0 = System.currentTimeMillis();
        List<String> categories = List.of(
                "kfc", "school", "university", "pharmacy", "apartments", "lotteria", "Jollibee",
                "McDonalds", "Burger King", "Pizzahut", "Domino pizza", "pizza company", "popeyes",
                "Texas chicken", "pepperonis", "Alfresco", "pizza express", "fivestar", "Highland", "trung nguyen coffee",
                "TheCoffeeHouse", "Starbucks", "Phúc Long", "Cộng cafe", "milano", "gemini", "Guta cafe",
                "katinat", "Toco Toco", "Ding tea", "Bocapop", "Gongcha", "the alley", "miutea", "tiger sugar", "Phê La"
                );
        for (String category : categories) {
            log.info("Category: {}", category);
            String mapKey = "MAP_KEY_GOOGLE" + "_" + category.replace(" ", "-").toLowerCase();
            initMapBoundary(mapKey);
            for (double lat = mapBoundary.getLat1(); lat > mapBoundary.getLat2(); lat -= DISTANCE_UNIT) {
                for (double lon = mapBoundary.getLon1(); lon < mapBoundary.getLon2(); lon += DISTANCE_UNIT) {
                    if (lat == mapBoundary.getLat1() && lon == mapBoundary.getLon1()) {
                        lat = mapBoundary.getCurrentLat();
                        lon = mapBoundary.getCurrentLon();
                    }
                    try {
                        initMap(lat, lon, category);
                        handleScrolling();
                        handleData(category);
                    } catch (Exception e) {
                        log.error("Error while get data location {}-{}: {}", lat, lon, e.getMessage(), e);
                    } finally {
                        mapBoundary.setCurrentLatLon(lat, lon);
                        redisTemplate.opsForValue().set(mapKey, gson.toJson(mapBoundary));
                    }
                }
            }
        }
        log.info("Time execute: {}", System.currentTimeMillis() - start0);
    }

    private static void initMap(double lat, double lon, String category) {
        String url = String.format("https://www.google.com/maps/search/%s/@%f,%f,15z", category, lat, lon);
        driver.get(url);
    }

    private void handleData(String category) {
        long start0 = System.currentTimeMillis();

        Object result = doRetry(() -> driver.executeScript("return document.querySelector('div[role=\"feed\"]').querySelectorAll('a')"));
        if (Objects.isNull(result)) return;
        List<RemoteWebElement> webElements = new ArrayList<>((List<RemoteWebElement>) result);
        log.info("Pre data size: {}", webElements.size());
        int count = 0;
        for (RemoteWebElement webElement : webElements) {
            String name = webElement.getAttribute("aria-label");
            String href = webElement.getAttribute("href");
            if (Strings.isNotEmpty(href) && Strings.isNotEmpty(name) && href.contains("/maps/")) {
                try {
                    String[] params = href.split("!3d")[1].split("!4d");
                    double lat = Double.parseDouble(params[0]);
                    double lon = Double.parseDouble(params[1].split("!")[0]);
                    String address = getAddress(name, href);
                    GoogleLocationEntity locationEntity = new GoogleLocationEntity();
                    locationEntity.setCategory(category);
                    locationEntity.setLat(lat);
                    locationEntity.setLon(lon);
                    locationEntity.setName(name);
                    locationEntity.setAddress(address);
                    mongoTemplate.save(locationEntity);
                    count++;
                } catch (Exception e) {
                    log.error("Error while handle location: {}, {}", href, e.getMessage(), e);
                }
            }
        }
        log.info("Data size: {}", count);
        log.info("Time execute handle map: {}", System.currentTimeMillis() - start0);
    }

    private String getAddress(String name, String href) {
        String jsCommand = """
                    document.my_categories = Array.from(document.querySelector("a[href='%s']").parentElement.querySelectorAll('span[aria-hidden]')).filter(span => span.className==="")
                    document.my_category = document.my_categories.length > 1 ? document.my_categories[1] : document.my_categories[0];
                    return document.my_category.parentElement.querySelector('span :nth-child(2)').textContent;
                """;
        jsCommand = String.format(jsCommand, href);
        try {
            return (String) driver.executeScript(jsCommand);
        } catch (Exception e) {
            log.error("Not has address: {}", name);
            return null;
        }
    }

    private void initMapBoundary(String mapKey) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(mapKey))) {
            mapBoundary = gson.fromJson(redisTemplate.opsForValue().get(mapKey), MapBoundary.class);
        } else {
            mapBoundary = MapBoundary.builder()
                    .lat1(21.14343050391292).lon1(105.63903808593751)
                    .currentLat(21.14343050391292).currentLon(105.63903808593751)
                    .lat2(20.838277806058933).lon2(106.02081298828126).build();
        }
    }

    private static void handleScrolling() {
        log.info("Scrolling ...");
        long elementsLength = 0;
        int elementCount = 0;
        int retries = 2000;
        int wait = 200;
        while (elementCount <= retries / wait) {
            long temp = (long) doRetry(() -> driver.executeScript("return document.querySelector('div[role=\"feed\"]').querySelectorAll('a').length"));
            if (temp <= elementsLength) {
                elementCount++;
            } else {
                elementsLength = temp;
                elementCount = 0;
            }
            Utils.sleep(wait);
            driver.executeScript("document.querySelector('div[role=\"feed\"]').scrollBy(0,2000)");
        }
    }
}
