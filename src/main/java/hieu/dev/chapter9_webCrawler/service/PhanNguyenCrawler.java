package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PhanNguyenCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://pnf.vn/he-thong-cua-hang").get();
        document.select(".all-submenu-1 > li")
                .forEach(store -> {
                    String name = store.attr("data-name");
                    String address = store.attr("data-address");
                    double lat = Double.parseDouble(store.attr("data-lat"));
                    double lon = Double.parseDouble(store.attr("data-long"));
                    BaseEntity entity = BaseEntity.builder()
                            .name(name).address(address).lon(lon).lat(lat).build();
                    System.out.println(entity);
                    mongoTemplate.save(entity, "phan_nguyen_places");
                });
    }
}
