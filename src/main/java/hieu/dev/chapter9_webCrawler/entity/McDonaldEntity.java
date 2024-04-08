package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document("mc_donald_places")
@Slf4j
public class McDonaldEntity extends BaseEntity {
    public static McDonaldEntity from(Element storeDiv) {
        String name = storeDiv.getElementsByClass("tname-store").get(0).text();
        String address = storeDiv.getElementsByClass("tcontent").get(0).text();
        try {
            String href = storeDiv.selectFirst("a[href]").absUrl("href");
            String latLonStr = href.split("/")[href.split("/").length - 1];
            String[] latLon = latLonStr.split(",");
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);

            McDonaldEntity mcDonaldEntity = new McDonaldEntity();
            mcDonaldEntity.setName(name);
            mcDonaldEntity.setAddress(address);
            mcDonaldEntity.setLat(lat);
            mcDonaldEntity.setLon(lon);
            return mcDonaldEntity;
        } catch (Exception e) {
            log.error("Error while handle store: {}", name);
            return null;
        }
    }
}
