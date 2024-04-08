package hieu.dev.chapter9_webCrawler.dto;

import hieu.dev.chapter9_webCrawler.entity.GovPlacePostCodeEntity;
import lombok.Data;

import java.util.List;

@Data
public class GovSearchResponse {
    private String Code;
    private String Message;
    private Integer total;
    private List<GovPlacePostCodeEntity> Places;
}
