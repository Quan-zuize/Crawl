package hieu.dev.chapter9_webCrawler.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
    private String id;
    private String name;
    private String address;
    private Double lat;
    private Double lon;
    private String placeCode;
    private Integer page;
    private boolean google;

    public static BaseEntity fromGongCha(JsonElement element) {
        JsonObject locationObject = element.getAsJsonObject();
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setId(locationObject.get("id").getAsString());
        baseEntity.setName(locationObject.get("name").getAsString());
        baseEntity.setAddress(locationObject.get("address").getAsString());
        baseEntity.setLat(locationObject.get("lat").getAsDouble());
        baseEntity.setLon(locationObject.get("lng").getAsDouble());
        return baseEntity;
    }

    public static BaseEntity fromKidPlaza(Element element) {
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setId(element.attr("data-source-code"));
        Elements attrElements = element.getElementsByTag("span");
        baseEntity.setAddress(attrElements.get(0).text());
        baseEntity.setLat(Double.valueOf(attrElements.get(2).attr("data-latitude")));
        baseEntity.setLon(Double.valueOf(attrElements.get(2).attr("data-longitude")));
        System.out.println(baseEntity);
        return baseEntity;
    }
}
