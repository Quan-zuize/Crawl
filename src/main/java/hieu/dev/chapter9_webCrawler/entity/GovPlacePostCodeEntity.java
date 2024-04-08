package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("gov_postcode_places")
public class GovPlacePostCodeEntity {
    @Id
    private String refPlaceId;
    @Field("lat")
    private Double latitude;
    @Field("lon")
    private Double longitude;
    @Field("name")
    private String label;

    private String idPrefix;
}
