package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("gov_places_v2")
public class GovPlaceEntity {
    @Id
    private Long id;
    @Field("lat")
    private Double latitude;
    @Field("lon")
    private Double longitude;
    private Integer category;
    private Long ndasId;
    private Long postcode;
    private String placeType;
    @Field("name")
    private String label;
    private String vnCode;
    public void formatId() {
        this.id = Long.valueOf(String.format("%05d%05d", this.postcode, this.ndasId));
    }
}
