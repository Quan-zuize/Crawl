package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;

@Data
public class OSMAddress {
    private String amenity;
    private String highway;
    private String shop;
    private String house_number;

    private String road;
    private String quarter;
    private String suburb;
    private String city;
    private String postcode;
    private String country;
    private String country_code;
}
