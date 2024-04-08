package hieu.dev.chapter9_webCrawler.dto;

import lombok.Data;

import java.util.List;

@Data
public class GovEncodeResponse {
    private String code;
    private String message;
    private Result result;

    @Data
    public static class Result {
        private Location max;
        private Location min;
        private Location center;
        private Location location;
        private String smartCode;
        private String postCode;
        private List<Address> addressCompnent;
        private String compoundCode;
    }

    @Data
    public static class Location {
        private String lat;
        private String lon;
    }

    @Data
    public static class Address {
        private String name;
        private String code;
        private String type;
    }
}
