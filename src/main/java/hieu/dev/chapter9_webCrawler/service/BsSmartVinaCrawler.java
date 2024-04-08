package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class BsSmartVinaCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://bsmartvina.com/bsmart_store/vn").get();
        document.getElementsByClass("promo_list_item_content_store").forEach(storeElement -> {
            try {
                String name = storeElement.getElementsByClass("promo_content_box1_store").text();
                String address = storeElement.getElementsByClass("promo_content_box2_store").text();
                String coordsLink = storeElement.select("a[href]").first().absUrl("href");
                Document coordsDocument = Jsoup.connect(coordsLink).get();
                String googleLink = getGoogleLink(coordsDocument);
                List<Double> coordinates = Utils.handleCoordinatesByUrl(googleLink);
                if(CollectionUtils.isEmpty(coordinates)) return;
                double lat = coordinates.get(0);
                double lon = coordinates.get(1);

                BaseEntity entity = BaseEntity.builder()
                        .name(name).address(address).lat(lat).lon(lon).build();
                System.out.println(entity);
                mongoTemplate.save(entity, "bs_smart_vina_places");
            } catch (Exception e) {
                log.error("Error {}, {}", storeElement, e.getMessage());
            } finally {
                Utils.sleep(100);
            }

        });
    }

    private static String getGoogleLink(Document coordsDocument) {
        try {
            return coordsDocument.select("[src]").first().attr("src");
        } catch (Exception e) {
            try {
                return coordsDocument.select("[href]").first().attr("href");
            } catch (Exception e1) {
                return null;
            }
        }
    }
}
