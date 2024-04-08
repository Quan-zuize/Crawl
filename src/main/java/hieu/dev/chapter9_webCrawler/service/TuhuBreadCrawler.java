package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TuhuBreadCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://tuhubread.com.vn/").get();
        Map<String, String> googleLinkMap = document.select(".tab-content > div").stream()
                .filter(element -> !element.select("iframe[src]").isEmpty())
                .collect(Collectors.toMap(
                        Element::id,
                        element -> element.selectFirst("iframe[src]").attr("src"))
                );

        document.select(".list_address a[href]")
                .forEach(store -> {
                    String id = store.attr("href").replace("#", "").trim();
                    String googleLink = googleLinkMap.get(id);
                    String name = store.selectFirst("span.add").text();
                    List<Double> coordinates = Utils.handleCoordinatesByUrl(googleLink);
                    if(!CollectionUtils.isEmpty(coordinates)) {
                        BaseEntity entity = BaseEntity.builder()
                                .name(name).lat(coordinates.get(0)).lon(coordinates.get(1)).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "tuhu_bread");
                    }
                });
    }
}
