package hieu.dev.chapter9_webCrawler.dto;

import lombok.Data;

@Data
public class LongChauPharmacyRequest {
    private String locationSlug;
    private int maxResult;
    private int skipCount;


    public LongChauPharmacyRequest(String locationSlug) {
        this.locationSlug = locationSlug;
        this.maxResult = 1000;
        this.skipCount = 0;
    }
}
