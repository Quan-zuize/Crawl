package hieu.dev.chapter9_webCrawler.selenium;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.MBCEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.Utils.doRetry;

@Slf4j
@Service
public class MbcSeleniumCrawler extends BaseSeleniumCrawler {
    public static final List<String> provinceCodes = Arrays.asList("90, 78, 26, 23, 97, 16, 86, 55, 75, 67, 77, 98, 94, 21, 50, 63, 65, 32, 76, 81, 61, 20, 18, 10, 45, 03 Hải Dương, 04 Hải Phòng, 95, 70, 36, 17, 57, 91, 60, 30, 66, 25, 31, 82, 07 Nam Định, 43, 08 Ninh Bình, 59, 35, 56, 47, 51, 53, Quảng Ninh, 48, 96, 34, 80, 06 Thái Bình, 24, 40, 49, 84, 87, 22, 85, 15, 33".split(", "));
    @Autowired
    MongoTemplate mongoTemplate;

//    @PostConstruct
    public void crawlPostcodes() {
        long start = System.currentTimeMillis();

        driver.get("https://mabuuchinh.vn/");
        for (int index = 0; index < provinceCodes.size(); index++) {
            long start0 = System.currentTimeMillis();

            String provinceCode = provinceCodes.get(index);
            WebElement findElement = doRetry(() -> driver.findElement(By.cssSelector("input[type='text']")));
            findElement.clear(); Utils.sleep(100);
            findElement.sendKeys(provinceCode);

            WebElement submitElement = doRetry(() -> driver.findElement(By.cssSelector("input[type='submit']")));
            submitElement.click();

            MBCEntity lv1PostCode = null;
            for (int page = 1; ; page++) {
                WebElement pagesContainer = doRetry(() -> driver.findElement(By.cssSelector("div[class='text-center'][style]")));
                List<WebElement> h4List = driver.findElements(By.cssSelector("h4"));
                if (page == 1) {
                    WebElement lv1Element = h4List.remove(0);
                    lv1PostCode = MBCEntity.build(lv1Element);
                    lv1PostCode.setPostCodes(new ArrayList<>());
                }
                lv1PostCode.getPostCodes().addAll(h4List.stream().map(MBCEntity::build).toList());

                if (hasPage(pagesContainer,  page + 1)) {
                    WebElement nextPageElement = getPageElement(pagesContainer,  page + 1);
                    nextPageElement.click();
                } else {
                    break;
                }
            }
            lv1PostCode.setIndex(index + 1);
            log.info("Saving post code data: {} - {}.{} - {}. Time execute: {}",  lv1PostCode.getCode(),  lv1PostCode.getIndex(),  lv1PostCode.getName(),  lv1PostCode.getPostCodes().size(),  System.currentTimeMillis() - start0);
            mongoTemplate.save(lv1PostCode);
        }

        log.info("Time execute: {}",  System.currentTimeMillis() - start);
    }

    private static boolean hasPage(WebElement pagesContainer,  int page) {
        return !pagesContainer.findElements(By.cssSelector(String.format("input[value='%d']",  page))).isEmpty();
    }

    private static WebElement getPageElement(WebElement pagesContainer,  int page) {
        return pagesContainer.findElement(By.cssSelector(String.format("input[value='%d']",  page)));
    }
}
