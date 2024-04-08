package hieu.dev.chapter9_webCrawler.dto;

import hieu.dev.chapter9_webCrawler.entity.GovPlaceEntity;
import lombok.Data;

import java.util.List;

@Data
public class GovReverseResponse {
    private String Code;
    private String Message;
    private Integer total;
    private List<GovPlaceEntity> Places;
}
