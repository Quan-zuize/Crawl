package hieu.dev.chapter9_webCrawler.model;

import lombok.Data;

@Data
public class GovPostCodeTrace {
    private String postCode;
    private String placeIdPrefix;

    public GovPostCodeTrace() {
        this.postCode = "0";
        this.placeIdPrefix = "0";
    }
}
