package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("google_places")
public class GoogleLocationEntity {
    @Id
    private String id;
    private String category;
    private Double lat;
    private Double lon;
    private String name;
    private String address;
}
