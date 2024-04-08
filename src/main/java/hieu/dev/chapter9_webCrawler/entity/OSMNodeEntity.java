package hieu.dev.chapter9_webCrawler.entity;

import com.mongodb.client.model.geojson.Point;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document("osm_places")
public class OSMNodeEntity {
    @Id
    private String id;
    private String place_id;
    private String osm_id;
    private String osm_type;
    private String type;
    private String addresstype;
    private String name;
    private String display_name;
    private Map<String, String> address;
    private List<String> boundingbox;
    private Point location;
}
