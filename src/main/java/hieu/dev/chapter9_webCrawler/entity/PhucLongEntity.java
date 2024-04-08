package hieu.dev.chapter9_webCrawler.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("phuc_long_places")
public class PhucLongEntity {
    private String id;
    private String name;
    private String address;
    private Double lat;
    private Double lon;
}
