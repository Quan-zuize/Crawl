package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.dto.LongChauPharmacyItemResponse;
import hieu.dev.chapter9_webCrawler.entity.LongChauPharmacyEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static hieu.dev.chapter9_webCrawler.client.LongChauPharmacyHttpClient.*;

@Service
@Slf4j
public class LongChauPharmacyCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
//    @PostConstruct
    public void crawlLongChauPharmacy() {
        List<String> provinces = getInitialProvinces();
        provinces.forEach(province -> {
            try {
                String body = getLocationsByProvince(province);
                JsonObject response = gson.fromJson(body, JsonObject.class);
                String itemsStr = response.getAsJsonArray("items").toString();
                List<LongChauPharmacyItemResponse> items = gson.fromJson(itemsStr, new TypeToken<List<LongChauPharmacyItemResponse>>(){}.getType());
                List<LongChauPharmacyEntity> entities = items.stream().map(LongChauPharmacyItemResponse::toEntity).toList();
                mongoTemplate.insertAll(entities);
                log.info("Handled {} {} locations", entities.size(), province);
            } catch (Exception e) {
                log.info("Error while get data from {}: {}", province, e.getMessage(), e);
            } finally {
                Utils.sleep(100);
            }
        });
    }
}
