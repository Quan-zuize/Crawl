package hieu.dev.chapter9_webCrawler.dto;

import com.google.gson.annotations.SerializedName;
import hieu.dev.chapter9_webCrawler.model.FoodyCity;
import lombok.Data;

import java.util.List;

@Data
public class FoodyCityResponse {
    @SerializedName("AllLocations")
    private List<FoodyCity> allLocations;
}
