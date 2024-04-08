package hieu.dev.chapter9_webCrawler.dto;

import hieu.dev.chapter9_webCrawler.entity.MilanoEntity;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.unbescape.html.HtmlEscape;

import java.util.List;

@Data
public class MilanoItemResponse {
    private String id;
    private String title;
    private List<String> latLng;

    public MilanoEntity toEntity() {
        MilanoEntity entity = new MilanoEntity();
        entity.setId(id);
        entity.setTitle(HtmlEscape.unescapeHtml(title));
        if(!CollectionUtils.isEmpty(latLng) && latLng.size() == 2) {
            entity.setLat(Double.valueOf(latLng.get(0)));
            entity.setLon(Double.valueOf(latLng.get(1)));
        }
        return entity;
    }
}
