package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("milano_coffee_places")
public class MilanoEntity {
    @Id
    private String id;
    private String title;
    private Double lat;
    private Double lon;
}
