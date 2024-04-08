package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.GovPostCodeHttpClient;
import hieu.dev.chapter9_webCrawler.dto.GovSearchResponse;
import hieu.dev.chapter9_webCrawler.entity.GovPlacePostCodeEntity;
import hieu.dev.chapter9_webCrawler.model.GovPostCodeTrace;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GovPostCodeCrawler {
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String MAP_TRACE_ID_KEY = "MAP_TRACE_ID";
    public static GovPostCodeTrace trace;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MongoTemplate mongoTemplate;

//    @PostConstruct
    public void initData() {
        List<String> lv1PostCodes = List.of("11-14", "50", "71-74");
        for (String lv1PostCode : lv1PostCodes) {
            String key = MAP_TRACE_ID_KEY + ":" + lv1PostCode;

            generateTrace(lv1PostCode);
            List<String> postCodes = GovPostCodeHttpClient.getPostCodes(lv1PostCode);

            postCodes.forEach(postCode -> {
                if(trace.getPostCode().compareTo(postCode) > 0) return;
                log.info("Handle postCode {}", postCode);

                List<String> listIdPrefix = GovPostCodeHttpClient.getListIdPrefix(postCode);
                for (String idPrefix : listIdPrefix) {
                    if(trace.getPlaceIdPrefix().compareTo(idPrefix) > 0) continue;
                    GovSearchResponse govSearchResponse = Utils.doRetry(() -> GovPostCodeHttpClient.callApiSearchAndDecrypt(idPrefix));
                    if (Objects.isNull(govSearchResponse) || CollectionUtils.isEmpty(govSearchResponse.getPlaces())) return;

                    govSearchResponse.getPlaces().forEach(entity -> {
                        GovPlacePostCodeEntity response = Utils.doRetry(() -> GovPostCodeHttpClient.callApiPlaceAndDecrypt(entity.getRefPlaceId(), idPrefix));
                        entity.setLongitude(response.getLongitude());
                        entity.setLatitude(response.getLatitude());
                        entity.setIdPrefix(response.getIdPrefix());
                        savePlace(entity);
                    });
                    trace.setPostCode(postCode);
                    trace.setPlaceIdPrefix(idPrefix);
                    redisTemplate.opsForValue().set(key, gson.toJson(trace));
                }
            });
        }

    }

    private void generateTrace(String lv1PostCode) {
        String key = MAP_TRACE_ID_KEY + ":" + lv1PostCode;
        if(Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            trace = gson.fromJson(redisTemplate.opsForValue().get(key), GovPostCodeTrace.class);
        } else {
            trace = new GovPostCodeTrace();
            redisTemplate.opsForValue().set(key, gson.toJson(trace));
        }
    }

    private void savePlace(GovPlacePostCodeEntity govPlacePostCodeEntity) {
        try {
            if(!mongoTemplate.exists(Query.query(Criteria.where("name").is(govPlacePostCodeEntity.getLabel())), GovPlacePostCodeEntity.class)) {
                mongoTemplate.save(govPlacePostCodeEntity);
            }
        } catch (Exception e) {
            log.error("Error while insert to mongodb: {}", gson.toJson(govPlacePostCodeEntity));
        }
    }
}
