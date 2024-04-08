package hieu.dev.chapter9_webCrawler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class JsoupTutorial {
    public static Cache<String, String> visited = Caffeine.newBuilder().build();

    public static void main(String[] args) {
        String seedUrl = "https://www.mongodb.com/docs/manual/tutorial/measure-index-use/";
        crawl(seedUrl, 1);
    }

    public static void crawl(String url, int maxDeep) {
        long start0 = System.currentTimeMillis();
        crawl(url, 0, maxDeep);
        log.info("Time execute: {}", System.currentTimeMillis() - start0);
    }

    public static void crawl(String url, int deep, int maxDeep) {
        if (deep > maxDeep) return;
        Optional<Document> pageOptional = getPage(url);
        if (pageOptional.isEmpty()) return;

        List<String> elementUrls = urlSeedAndFrontier(pageOptional.get());

        for (String elementUrl : elementUrls) {
            crawl(elementUrl, deep + 1, maxDeep);
        }
    }

    public static List<String> urlSeedAndFrontier(Document document) {
        return document.select("div#bodyContent").select("a[href]")
                .stream().map(element -> element.absUrl("href"))
                .filter(Objects::nonNull)
                .filter(elementUrl -> !visited.asMap().containsKey(elementUrl))
                .toList();
    }

    public static Optional<Document> getPage(String url) {
        log.info("Get page from: {}", url);
        try {
            Connection connect = Jsoup.connect(url);
            Document document = connect.get();
            if (Objects.equals(connect.response().statusCode(), 200)) {
                log.info("Link {}, title: {}", url, document.title());
                visited.put(url, document.title());
            }
            return Optional.of(document);
        } catch (IOException e) {
            log.error("Error while get page url {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }
}
