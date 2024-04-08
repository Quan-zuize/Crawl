package hieu.dev.chapter9_webCrawler.selenium;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.PhucLongEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.Utils.doRetry;

@Slf4j
@Service
public class PhucLongCrawler extends BaseSeleniumCrawler {
    @Autowired
    MongoTemplate mongoTemplate;

//    @PostConstruct
    public void crawlPhucLong() {
        long start0 = System.currentTimeMillis();
        driver.get("https://www.google.com/maps");
        try (Workbook workbook = new XSSFWorkbook("/home/hieu/Documents/project/SystemDesignInterview/src/main/java/hieu/dev/chapter9_webCrawler/selenium/phuclong.xlsx")) {
            Sheet sheet = workbook.sheetIterator().next();
            Iterator<Row> rowIterator = sheet.rowIterator();

            int count = 1;
            while (rowIterator.hasNext()) {
                String name = "";
                Row row = rowIterator.next();
                if (row.getRowNum() < 9) continue;

                try {
                    name = row.getCell(2).getStringCellValue();
                    if(Strings.isEmpty(name)) return;

                    log.info("Handle location: {}.{}", count, name);
                    String address = row.getCell(3).getStringCellValue();

                    String currentUrl = Utils.getSearchUrl(driver, address);

                    List<Double> coordinates = Utils.handleCoordinatesByUrl(currentUrl);
                    if (!CollectionUtils.isEmpty(coordinates)) {
                        double lat = coordinates.get(0);
                        double lon = coordinates.get(1);

                        PhucLongEntity entity = PhucLongEntity.builder()
                                .name(name).address(address).lat(lat).lon(lon)
                                .build();
                        mongoTemplate.save(entity);
                    }

                } catch (Exception e) {
                    log.error("Error while crawl {}: {}", name, e.getMessage(), e);
                } finally {
                    count++;
                }
            }
        } catch (IOException e) {
            log.error("Error Internal: {}", e.getMessage(), e);
        } finally {
            log.info("Time execute: {}", System.currentTimeMillis() - start0);
        }
    }
}
