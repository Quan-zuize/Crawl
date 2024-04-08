package hieu.dev.chapter9_webCrawler.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class ShopeefoodCity {
    private String id;
    private String name;
    private List<ShopeefoodDistrict> districts;
  
    @Data
    private static class ShopeefoodDistrict {
        private String name;
        @SerializedName("district_id")
        private String districtId;
    }
}
