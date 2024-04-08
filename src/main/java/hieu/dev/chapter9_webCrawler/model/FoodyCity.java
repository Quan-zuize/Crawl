package hieu.dev.chapter9_webCrawler.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class FoodyCity {
    @SerializedName("Id")
    private Integer id;
    @SerializedName("countryId")
    private Integer countryId;
    @SerializedName("Name")
    private String name;
    @SerializedName("UrlRewriteName")
    private String urlRewriteName;
}
