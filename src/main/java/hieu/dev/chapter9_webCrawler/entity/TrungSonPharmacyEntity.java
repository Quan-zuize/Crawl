package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static hieu.dev.chapter9_webCrawler.Utils.getCoordinatesByUrl;

@Data
@Document("trung_son_pharmacy_entity")
public class TrungSonPharmacyEntity {
    private String id;
    private String name;
    private String address;
    private Double lat;
    private Double lon;

    public static TrungSonPharmacyEntity from(ChromeDriver driver, Element storeDiv){
        TrungSonPharmacyEntity entity = new TrungSonPharmacyEntity();

        Elements dataElements = storeDiv.select("span");
        String name = dataElements.get(1).text();
        String address = dataElements.get(3).text();
        entity.setName(name);
        entity.setAddress(address);

        String href = storeDiv.selectFirst("a[href]").absUrl("href");
        List<Double> coordinates = getCoordinatesByUrl(driver, href);
        if(!CollectionUtils.isEmpty(coordinates)) {
            double lat = coordinates.get(0);
            double lon = coordinates.get(1);

            entity.setLat(lat);
            entity.setLon(lon);
        }
        return entity;
    }
}
