package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hieu.dev.chapter9_webCrawler.client.OSMHttpClient;
import hieu.dev.chapter9_webCrawler.model.OSMMetaData;
import hieu.dev.chapter9_webCrawler.entity.OSMNodeEntity;
import hieu.dev.chapter9_webCrawler.dto.OSMReverseResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OSMCrawler {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @PostConstruct
    public void mapData() {
        String inputFilePath = "map_data/vietnam-latest.osm";
        handleWayData(inputFilePath);
        handleNodeData(inputFilePath);
    }

    public void handleWayData(String inputFilePath) {
        log.info("Handling way data");
        long start0 = System.currentTimeMillis();
        try (FileInputStream inputStream = new FileInputStream(inputFilePath)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int eventType = reader.next();

                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    if (elementName.equals("nd")) {
                        String wayPlaceId = reader.getAttributeValue(null, "ref");
                        redisTemplate.opsForSet().add("WAY_PLACE_IDS", wayPlaceId);
                    }
                }
            }

            reader.close();
        } catch (Exception e) {
            log.error("Error while handle way data: {}", e.getMessage(), e);
        } finally {
            log.info("Time execute: {}", System.currentTimeMillis() - start0);
        }
    }

    public void handleNodeData(String inputFilePath) {
        log.info("Handling node data");
        long start0 = System.currentTimeMillis();
        try (FileInputStream inputStream = new FileInputStream(inputFilePath)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);

            List<OSMMetaData> osmMetaDataList = new ArrayList<>();
            while (reader.hasNext()) {
                int eventType = reader.next();

                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    if (elementName.equals("node")) {
                        OSMMetaData osmMetaData = OSMMetaData.build(reader, "N");
                        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("WAY_PLACE_IDS", osmMetaData.getOsmId()))) {
                            continue;
                        }
                        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("BOUND_PLACE_IDS", osmMetaData.getOsmId()))) {
                            continue;
                        }
                        if (mongoTemplate.exists(Query.query(Criteria.where("location").is(new Point(osmMetaData.getLon(), osmMetaData.getLat()))), OSMNodeEntity.class)) {
                            continue;
                        }

                        osmMetaDataList.add(osmMetaData);


                        if (osmMetaDataList.size() == 50 || !reader.hasNext()) {
                            List<String> osmIds = osmMetaDataList.stream().map(OSMMetaData::getOsmId).toList();
                            log.info("Handle bulk size {}, {}", osmMetaDataList.size(), gson.toJson(osmIds));

                            List<String> formatOsmIds = osmMetaDataList.stream().map(OSMMetaData::getFormatOsmId).toList();
                            List<OSMReverseResponse> osmReverseResponses = OSMHttpClient.lookupDataByIds(formatOsmIds);

                            List<String> successOsmIds = osmReverseResponses.stream().map(OSMReverseResponse::getOsm_id).toList();
                            List<String> failedOsmIds = osmIds.stream()
                                    .filter(id -> !successOsmIds.contains(id))
                                    .toList();
                            log.warn("Bound way place id size {}, {}", failedOsmIds.size(), gson.toJson(failedOsmIds));
                            if (!CollectionUtils.isEmpty(failedOsmIds)) {
                                redisTemplate.opsForSet().add("BOUND_PLACE_IDS", failedOsmIds.toArray(new String[0]));
                            }

                            osmReverseResponses.stream()
                                    .filter(Objects::nonNull)
                                    .filter(osmReverseResponse -> Objects.nonNull(osmReverseResponse.getPlace_id()))
                                    .map(osmReverseResponse -> osmReverseResponse.toOSMNodeEntity(osmMetaDataList))
                                    .forEach(mongoTemplate::save);
                            osmMetaDataList.clear();
                        }
                    }
                }
            }
            reader.close();
            redisTemplate.unlink(List.of("WAY_PLACE_IDS", "BOUND_PLACE_IDS"));
        } catch (Exception e) {
            log.error("Error while node data: {}", e.getMessage(), e);
        } finally {
            log.info("Time execute: {}", System.currentTimeMillis() - start0);
        }
    }
}
