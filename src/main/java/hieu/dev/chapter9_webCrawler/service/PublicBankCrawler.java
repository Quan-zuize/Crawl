package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.TocotocoEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.Utils.gson;
import static hieu.dev.chapter9_webCrawler.Utils.handleCoordinatesByUrl;

@Service
@Slf4j
public class PublicBankCrawler extends BaseSeleniumCrawler {
    public ChromeDriver googleChrome;

    public void crawl() throws IOException {
        googleChrome = new ChromeDriver();
        googleChrome.get("https://www.google.com/maps");
        for (int type = 1; type <= 2; type++) {
            for (int page = 1; ; page++) {
                String url = UriComponentsBuilder.fromHttpUrl("https://www.publicbank.com.vn/Location/ListLocation")
                        .queryParam("page", page)
                        .queryParam("locationType", type).build().toUriString();

                log.info("Path: {}", url);
                Document document;
                try {
                    document = Jsoup.connect(url).get();
                } catch (Exception e) {
                    log.info("End page");
                    break;
                }
                Elements elements = document.getElementsByClass("media-body");
                if (elements.isEmpty()) break;
                elements.forEach(element -> {
                    String name = element.getElementsByTag("h4").text();
                    String address = element.getElementsByTag("td").text();
                    address = address.replace("Địa chỉ :", "").split("Điện thoại")[0].trim();
                    String href = element.getElementsByTag("a").attr("abs:href");
                    saveDataWithHref(address, name, href, "public_bank_places");
                });
                Utils.sleep(100);
            }
        }
    }

    public void saveDataWithHref(String address, String name, String url, String collectionName) {
        if (Strings.isEmpty(url)) {
            saveData(address, name, collectionName);
            return;
        }
        String currentUrl = getNewUrl(driver, url);
        List<Double> coordinates = getCoordinatesByUrl(currentUrl);

        if (CollectionUtils.isEmpty(coordinates)) {
            currentUrl = Utils.getSearchUrl(googleChrome, address);
            coordinates = getCoordinatesByUrl(currentUrl);
        }

        if (!CollectionUtils.isEmpty(coordinates)) {
            double lat = coordinates.get(0);
            double lon = coordinates.get(1);

            TocotocoEntity entity = new TocotocoEntity();
            entity.setAddress(address);
            entity.setName(name);
            entity.setLat(lat);
            entity.setLon(lon);
            mongoTemplate.save(entity, collectionName);
            log.info("Place: {}", gson.toJson(entity));
            Utils.sleep(500);
        }
    }

    private static List<Double> getCoordinatesByUrl(String url) {
        try {
            String decodeUrl = URLDecoder.decode(url);
            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromHttpUrl(decodeUrl).build().getQueryParams();
            String coordinateStr = queryParams.get("ll").get(0);
            String[] coordinate = coordinateStr.split(",");
            return List.of(Double.parseDouble(coordinate[0]), Double.parseDouble(coordinate[1]));
        } catch (Exception e) {
            return handleCoordinatesByUrl(url);
        }
    }

    public static void main(String[] args) {
        System.out.println(getCoordinatesByUrl("https://www.google.com/maps/d/u/0/viewer?mid=1UnMAkhJW8DVKE54deP1eNZaiVW-25AYV&ll=20.970730000000003%2C105.87879&z=17"));
    }

    public static String getNewUrl(ChromeDriver driver, String oldUrl) {
        driver.get(oldUrl);
        String currentUrl = driver.getCurrentUrl();
        while (!currentUrl.contains("3d!") && !currentUrl.contains("&ll=") && !currentUrl.contains("/@") && !currentUrl.contains("/signin/")) {
            currentUrl = driver.getCurrentUrl();
            Utils.sleep(500);
            log.info("Retry ...");
        }
        return currentUrl;
    }
}
