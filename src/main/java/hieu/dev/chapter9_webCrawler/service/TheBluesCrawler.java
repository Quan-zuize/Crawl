package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TheBluesCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://theblues.com.vn/store/").get();
        document.select(".cl_result_wrap > div")
                .forEach(store -> {
                    String name = store.getElementsByTag("h3").first().text();
                    String address = store.getElementsByTag("p").first().text();
                    if(store.hasAttr("data-lat") && Strings.isNotEmpty(store.attr("data-lat")) && Double.parseDouble(store.attr("data-lat")) != 0){
                        double lat = Double.parseDouble(store.attr("data-lat"));
                        double lon = Double.parseDouble(store.attr("data-lng"));
                        BaseEntity entity = BaseEntity.builder()
                                .name(name).address(address).lon(lon).lat(lat).build();
                        System.out.println(entity);
                        mongoTemplate.save(entity, "the_blues_places");
                    } else {
                        saveData(address, name, "the_blues_places");
                    }
                });
    }
}
