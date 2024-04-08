package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BatdongsanCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    private static final String BDS_REDIS_TRACE_SET = "BDS_TRACE_SET:";
    public void crawl() throws IOException {
        List<String> urls = List.of("https://batdongsan.com.vn/nha-dat-ban", "https://batdongsan.com.vn/nha-dat-cho-thue");

        urls.forEach(url -> {
            for (int page = 1; ; page++) {
                if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(BDS_REDIS_TRACE_SET + url, String.valueOf(page)))){
                    continue;
                }
                String pageUrl = url + "/p" + page;
                long start0 = System.currentTimeMillis();
                ChromeDriver chromeDriver = getChromeDriver();
                try {
                    chromeDriver.get(pageUrl);
                    if (chromeDriver.findElements(By.className("re__pagination-group")).isEmpty()) return;
                    int finalPage = page;
                    List<BaseEntity> entities = chromeDriver.findElements(By.cssSelector("a.js__product-link-for-product-id[href]"))
                            .stream().map(store -> {
                                String name = store.findElement(By.className("js__card-title")).getText();
                                String address = store.findElement(By.className("re__card-location")).getText().replace("·", "").trim();
                                String storeLink = store.getAttribute("href");
                                List<Double> coordinates = getCoordinates(storeLink);
                                if (Objects.isNull(coordinates)) return null;

                                BaseEntity entity = BaseEntity.builder()
                                        .name(name).address(address).lat(coordinates.get(0)).lon(coordinates.get(1))
                                        .placeCode(url)
                                        .page(finalPage)
                                        .build();
                                log.info("place: {}", entity);
                                return entity;
                            }).filter(Objects::nonNull).toList();

                    entities.forEach(entity -> mongoTemplate.save(entity, "batdongsan_places"));
                    redisTemplate.opsForSet().add(BDS_REDIS_TRACE_SET + url, String.valueOf(page));
                } catch (Exception e) {
                    log.error("Error while handle url: {}", pageUrl);
                } finally {
                    chromeDriver.quit();
                    log.info("Success handle page: {}ms-{}", System.currentTimeMillis() - start0, pageUrl);
                    Utils.sleep(100);
                }
            }

        });
    }

    private static List<Double> getCoordinates(String storeLink) {
        ChromeDriver chromeDriver = getChromeDriver();
        try {
            chromeDriver.get(storeLink);
            String googleLink = chromeDriver.findElement(By.cssSelector(".re__pr-map iframe[data-src]")).getAttribute("data-src");
            return Utils.handleCoordinatesByUrl(googleLink);
        } catch (Exception e) {
            log.error("Error while crawl: {}", storeLink);
            return null;
        } finally {
            chromeDriver.quit();
            Utils.sleep(100);
        }
    }
}
