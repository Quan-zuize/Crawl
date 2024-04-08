package hieu.dev.chapter9_webCrawler.dto;

import hieu.dev.chapter9_webCrawler.entity.LongChauPharmacyEntity;
import lombok.Data;

@Data
public class LongChauPharmacyItemResponse {
    private String shopCode;
    private LongChauPharmacyLocation location;

    @Data
    public static class LongChauPharmacyLocation {
        private String address;
        private LongChauPharmacyCoordinates coordinates;
    }

    @Data
    public static class LongChauPharmacyCoordinates {
        private Double latitude;
        private Double longitude;
    }

    public LongChauPharmacyEntity toEntity() {
        LongChauPharmacyEntity entity = new LongChauPharmacyEntity();
        entity.setShopCode(shopCode);
        entity.setName(location.address);
        entity.setLat(location.coordinates.latitude);
        entity.setLon(location.coordinates.longitude);
        return entity;
    }
}
