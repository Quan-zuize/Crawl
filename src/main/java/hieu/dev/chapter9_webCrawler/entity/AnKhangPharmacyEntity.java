package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

@Data
@Document("an_khang_pharmacy_places")
public class AnKhangPharmacyEntity {
    private String id;
    private String name;
    private Double lat;
    private Double lon;

    public static AnKhangPharmacyEntity from(Element li) {
        AnKhangPharmacyEntity entity = new AnKhangPharmacyEntity();

        String id = li.attr("data-id");
        String address = getElementTextByClass(li, "txtl");
        entity.setId(id);
        entity.setName(address);

        String mapHref = getElementHrefByCssSelector(li, "a[href][class='map-link']");
        if(Strings.isNotEmpty(mapHref)) {
            Map<String, String> prams = UriComponentsBuilder.fromHttpUrl(mapHref).build().getQueryParams().toSingleValueMap();
            String query = prams.get("query");
            String[] coordinates = query.split(",");
            String lat = coordinates[0];
            String lon = coordinates[1];
            entity.setLat(Double.parseDouble(lat));
            entity.setLon(Double.parseDouble(lon));
        }

        return entity;
    }

    private static String getElementTextByClass(Element ancestor, String className) {
        Elements elementsByClass = ancestor.getElementsByClass(className);
        if(elementsByClass.isEmpty()) return null;
        return elementsByClass.get(0).text();
    }

    private static String getElementHrefByCssSelector(Element ancestor, String cssSelector) {
        Element element = ancestor.selectFirst(cssSelector);
        if(Objects.isNull(element)) return null;
        return element.attr("abs:href");
    }
}
