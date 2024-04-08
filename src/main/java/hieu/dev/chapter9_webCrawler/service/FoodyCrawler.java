package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.bulk.BulkWriteError;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.FoodyHttpClient;
import hieu.dev.chapter9_webCrawler.entity.FoodyLocationEntity;
import hieu.dev.chapter9_webCrawler.model.FoodyCity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class FoodyCrawler {
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private MongoTemplate mongoTemplate;

//    @PostConstruct
    public void crawlData() {
        long start0 = System.currentTimeMillis();
        List<String> cities = FoodyHttpClient.getListCity().stream().map(FoodyCity::getUrlRewriteName).toList();
//        List<String> cities = List.of("ha-noi", "ho-chi-minh", "da-nang");
        List<String> categories = FoodyHttpClient.getListCategories();
        for (String city : cities) {
            for (String category : categories) {
                for (int page = 1; page < 51; page++) {
                    try {
                        String urlCategory = city + "/" + category;
                        List<FoodyLocationEntity> locations = FoodyHttpClient.getLocations(urlCategory, page);
                        if(CollectionUtils.isEmpty(locations)) {
                            break;
                        }

                        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, FoodyLocationEntity.class);
                        locations.forEach(bulkOperations::insert);
                        bulkOperations.execute();
                    } catch (BulkOperationException e) {
                        List<String> list = e.getErrors().stream()
                                .map(BulkWriteError::getMessage)
                                .map(this::parseDuplicateErrorMessage)
                                .filter(Strings::isNotEmpty).toList();
                        log.error("Error while handle data {}-{}-{} size: {}, {}", city, category, page, e.getErrors().size(), gson.toJson(list));
                    } catch (Exception e) {
                        log.error("Error while handle data {}-{}-{}: size 12, {}", city, category, page, e.getMessage());
                    } finally {
                        Utils.sleep(500);
                    }
                }
            }
        }
        log.info("Time execute: {}", System.currentTimeMillis() - start0);
    }
    private String parseDuplicateErrorMessage(String msg) {
        String[] split = msg.split("_id:");
        if (split.length == 0) return "";
        return split[1].replace("}", "").trim();
    }
}
