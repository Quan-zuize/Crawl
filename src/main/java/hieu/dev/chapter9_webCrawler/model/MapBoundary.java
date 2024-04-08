package hieu.dev.chapter9_webCrawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapBoundary {
    private Double lat1;
    private Double lon1;
    private Double lat2;
    private Double lon2;
    private Double currentLat;
    private Double currentLon;
    public void setCurrentLatLon(Double currentLat, Double currentLon) {
        this.currentLat = currentLat;
        this.currentLon = currentLon;
    }
}
