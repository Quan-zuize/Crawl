package hieu.dev.chapter9_webCrawler.dto;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import hieu.dev.chapter9_webCrawler.entity.OSMNodeEntity;
import hieu.dev.chapter9_webCrawler.model.OSMMetaData;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class OSMReverseResponse {
    private String place_id;
    private String osm_id;
    private String osm_type;
    private String type;
    private String addresstype;
    private String name;
    private String display_name;
    private Map<String, String> address;
    private List<String> boundingbox;
    private Double lat;
    private Double lon;

    public OSMNodeEntity toOSMNodeEntity(List<OSMMetaData> osmMetaDataList) {
        OSMNodeEntity osmNodeEntity = new OSMNodeEntity();
        BeanUtils.copyProperties(this, osmNodeEntity);

        osmMetaDataList.stream()
                .filter(element -> Objects.equals(element.getOsmId(), osm_id))
                .findAny()
                .ifPresent(osmMetaData -> osmNodeEntity.setLocation(new Point(new Position(osmMetaData.getLon(), osmMetaData.getLat()))));
        return osmNodeEntity;
    }
}
