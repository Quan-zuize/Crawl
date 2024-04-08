package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.GovHttpClient;
import hieu.dev.chapter9_webCrawler.dto.GovReverseResponse;
import hieu.dev.chapter9_webCrawler.entity.GovPlaceEntity;
import hieu.dev.chapter9_webCrawler.model.MapBoundary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GovCrawler {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final Double DISTANCE_UNIT = 0.0017765142178717312;
    private static final String MAP_KEY = "MAP_KEY";
    private static MapBoundary mapBoundary;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;

//    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    private void logTime() {
        log.info("Calculate time remain: {} hours", GovHttpClient.calculateTimeHours(mapBoundary, DISTANCE_UNIT, 1));
        log.info("Calculate time remain: {} days", GovHttpClient.calculateTimeDays(mapBoundary, DISTANCE_UNIT, 1));
    }

//    @PostConstruct
    private void initData() {
        initMapBoundary();
        logTime();
        for (double lat = mapBoundary.getLat1(); lat > mapBoundary.getLat2(); lat -= DISTANCE_UNIT) {
            for (double lon = mapBoundary.getLon1(); lon < mapBoundary.getLon2(); lon += DISTANCE_UNIT) {
                if(lat == mapBoundary.getLat1() && lon == mapBoundary.getLon1()) {
                    lat = mapBoundary.getCurrentLat(); lon = mapBoundary.getCurrentLon();
                }
                try {
                    GovReverseResponse govReverseResponse = GovHttpClient.callApiVnPost(lat, lon);
                    govReverseResponse.getPlaces().forEach(govPlaceEntity -> {
                        govPlaceEntity.formatId();
                        savePlace(govPlaceEntity);
                    });
                } catch (Exception e) {
                    log.error("Error while get data location {}-{}: {}", lat, lon, e.getMessage(), e);
                } finally {
                    mapBoundary.setCurrentLatLon(lat, lon);
                    redisTemplate.opsForValue().set(MAP_KEY, gson.toJson(mapBoundary));
                    Utils.sleep(500);
                }
            }
        }
    }

    private void initMapBoundary() {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(MAP_KEY))) {
            mapBoundary = gson.fromJson(redisTemplate.opsForValue().get(MAP_KEY), MapBoundary.class);
        } else {
            mapBoundary = MapBoundary.builder()
                    .lat1(21.14343050391292).lon1(105.63903808593751)
                    .currentLat(21.14343050391292).currentLon(105.63903808593751)
                    .lat2(20.838277806058933).lon2(106.02081298828126).build();
        }
    }

    private void savePlace(GovPlaceEntity govPlaceEntity) {
        try {
            if(!mongoTemplate.exists(Query.query(Criteria.where("name").is(govPlaceEntity.getLabel())), GovPlaceEntity.class)) {
                mongoTemplate.save(govPlaceEntity);
            }
        } catch (Exception e) {
            log.error("Error while insert to mongodb: {}", gson.toJson(govPlaceEntity));
        }
    }
}
