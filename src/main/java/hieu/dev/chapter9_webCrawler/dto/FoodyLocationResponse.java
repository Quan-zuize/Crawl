package hieu.dev.chapter9_webCrawler.dto;

import com.google.gson.annotations.SerializedName;
import hieu.dev.chapter9_webCrawler.entity.FoodyLocationEntity;
import lombok.Data;

import java.util.List;

@Data
public class FoodyLocationResponse {
    @SerializedName("City")
    private String cityId;
    @SerializedName("Items")
    private List<FoodyLocationEntity> items;
    private List<FoodyLocationEntity> searchItems;
}
