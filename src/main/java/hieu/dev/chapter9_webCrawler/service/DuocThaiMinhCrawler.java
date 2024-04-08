package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DuocThaiMinhCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://duocthaiminh.vn/nha-thuoc").get();
        document.select(".districts > a[href]")
                .stream().map(a -> a.attr("href"))
                .forEach(pageLink -> {
                    log.info("Url: " + pageLink);
                    try {
                        Document districtDocument = Jsoup.connect(pageLink).get();
                        districtDocument.select(".system-store-item")
                                .forEach(store -> {
                                    String name = store.selectFirst(".system-store-item__name").text();
                                    String address = store.selectFirst(".system-store-item__desc").text();
                                    if (address.split(":").length > 1) {
                                        address = address.split(":")[1];
                                    }
                                    List<String> googleLinks = store.select("a[href]")
                                            .stream().filter(Objects::nonNull)
                                            .map(a -> a.attr("href"))
                                            .filter(Strings::isNotEmpty)
                                            .filter(a -> a.contains("https://www.google.com/maps"))
                                            .toList();
                                    try {
                                        String googleLink = googleLinks.get(0);
                                        String destination = UriComponentsBuilder.fromHttpUrl(googleLink).build().getQueryParams().toSingleValueMap().get("destination");
                                        double lat = Double.parseDouble(destination.split(",")[0].trim());
                                        if(lat == 0) throw new RuntimeException();
                                        double lon = Double.parseDouble(destination.split(",")[1].trim());
                                        BaseEntity entity = BaseEntity.builder().name(name).address(address).lon(lon).lat(lat).build();
                                        log.info("{}", entity);
                                        mongoTemplate.save(entity, "duoc_thai_minh_places");
                                    } catch (Exception e){
//                                        saveData(address, name, "duoc_thai_minh_places");
                                        log.info("Ignore: " + address);
                                    }

                                });
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                });

    }
}
