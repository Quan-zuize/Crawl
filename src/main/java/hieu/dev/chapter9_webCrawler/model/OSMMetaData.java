package hieu.dev.chapter9_webCrawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.stream.XMLStreamReader;

@AllArgsConstructor
@Data
public class OSMMetaData {
    private String osmId;
    private String osmType;
    private Double lon;
    private Double lat;

    public String getFormatOsmId() {
        return osmType + osmId;
    }

    public static OSMMetaData build(XMLStreamReader reader, String osmType) {
        String osmId = reader.getAttributeValue(null, "id");
        double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
        double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));

        return new OSMMetaData(osmId, osmType, lon, lat);
    }
}
