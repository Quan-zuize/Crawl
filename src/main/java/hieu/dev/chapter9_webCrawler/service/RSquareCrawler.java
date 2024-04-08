package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
public class RSquareCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        List<String> categoryUrls = List.of("https://vi.rsquare.vn/hochiminh_office/",
                "https://vi.rsquare.vn/hanoi_office/",
                "https://vi.rsquare.vn/northern_factory_vietnam/",
                "https://vi.rsquare.vn/southern_factory_vietnam/",
                "https://vi.rsquare.vn/northern_industrial_park_vietnam/",
                "https://vi.rsquare.vn/southern_industrial_park_vietnam/");
        categoryUrls.forEach(categoryUrl -> {
            Query query = Query.query(Criteria.where("placeCode").is(categoryUrl));
            query.with(Sort.by(Sort.Direction.DESC, "page"));
            BaseEntity tracingEntity = mongoTemplate.findOne(query, BaseEntity.class, "rsquare_places");
            int page = Objects.nonNull(tracingEntity) ? tracingEntity.getPage() : 1;
            for (; ; page++) {
                String url = UriComponentsBuilder.fromHttpUrl(categoryUrl)
                        .queryParam("page", page).toUriString();
                log.info("Crawling url page: {}", url);
                try {
                    Document document = Jsoup.connect(url).get();
                    Element currentPageElement = document.selectFirst("ul.pagination > li.active > a[href]");
                    if(Objects.nonNull(currentPageElement)) {
                        int currentPage = Integer.parseInt(UriComponentsBuilder.fromHttpUrl(currentPageElement.absUrl("href")).build().getQueryParams().toSingleValueMap().get("page"));
                        if (currentPage != page) return;
                    }

                    int finalPage = page;
                    document.select("a.post_link_wrap[href]").stream().map(place -> place.absUrl("href"))
                            .forEach(placeUrl -> {
                                try {
                                    log.info("Crawling url page: {}", placeUrl);
                                    String id = UriComponentsBuilder.fromHttpUrl(placeUrl).build().getQueryParams().toSingleValueMap().get("idx");

                                    Document placeDocument = Jsoup.connect(placeUrl).get();
                                    String name = placeDocument.selectFirst(".view_tit").ownText();
                                    String address = placeDocument.select("table.tableHorizontal").get(2).selectFirst("td").text();
                                    if(address.split("[|│]").length > 1) {
                                        address = address.split("[|│]")[1].trim();
                                    }
                                    String googleLink = placeDocument.select("iframe[src]").not("#hidden_frame").first().attr("src");

                                    saveDataWithHref(address, name, googleLink, id, categoryUrl, finalPage, "rsquare_places");
                                } catch (Exception e) {
                                    log.error("Error while handle place url: {} - {}", placeUrl, e.getMessage(), e);
                                } finally {
                                    int delta = new Random().nextInt(500);
                                    Utils.sleep(1000 + delta);
                                }
                            });
                    if (Objects.isNull(currentPageElement)) return;
                } catch (Exception e) {
                    log.error("Error while handle category: {} - {}", url, e.getMessage(), e);
                } finally {
                    Utils.sleep(1000);
                }
            }

        });

    }
}
