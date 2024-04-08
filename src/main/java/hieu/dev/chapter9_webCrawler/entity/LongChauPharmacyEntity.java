package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("long_chau_places")
public class LongChauPharmacyEntity {
    @Id
    private String shopCode;
    private String name;
    private Double lat;
    private Double lon;
}
