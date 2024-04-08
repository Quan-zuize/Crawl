package hieu.dev.chapter9_webCrawler.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("foody_places_v2")
public class FoodyLocationEntity {
    @Id
    @SerializedName("Id")
    public Integer id;
    @SerializedName("Name")
    public String name;
    @SerializedName("Address")
    public String address;
    @SerializedName("District")
    public String district;
    @SerializedName("City")
    public String city;
    @SerializedName("Latitude")
    public Double lat;
    @SerializedName("Longitude")
    public Double lon;
}
