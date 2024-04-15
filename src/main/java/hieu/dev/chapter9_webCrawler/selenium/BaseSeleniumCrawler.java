package hieu.dev.chapter9_webCrawler.selenium;

import hieu.dev.chapter9_webCrawler.CertUtils;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v118.network.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static hieu.dev.chapter9_webCrawler.Utils.*;

@Slf4j
public class BaseSeleniumCrawler {
    public static final String BASE_DIR = "/tmp/selenium_document/";
    public static final ChromeDriver driver;
    private static int i = 0;

    static {
        CertUtils.ignoreCertificates();
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.get("https://www.google.com/maps");
    }

    public static DevTools devTools() {
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        return devTools;
    }

    public static DevTools devTools(ChromeDriver driver) {
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        return devTools;
    }

    @Autowired
    protected MongoTemplate mongoTemplate;

    public void dropCollection(String collectionName) {

        mongoTemplate.dropCollection(collectionName);
    }
    public int tracePage0(String placeCode, String collectionName) {
        return tracePage(placeCode, 0, collectionName);
    }
    public int tracePage(String placeCode, String collectionName) {
        return tracePage(placeCode, 1, collectionName);
    }
    public int tracePage(String placeCode, int startPage, String collectionName) {
        Query query = new Query();
        if(Strings.isNotEmpty(placeCode)) {
            query = Query.query(Criteria.where("placeCode").is(placeCode));
        }
        query.with(Sort.by(Sort.Direction.DESC, "page"));
        BaseEntity tracingEntity = mongoTemplate.findOne(query, BaseEntity.class, collectionName);
        return Objects.nonNull(tracingEntity) ? tracingEntity.getPage() : startPage;
    }

    public void saveData(String address, String name, String collectionName) {
        address = preHandle(address);
        String google = preHandleGG(address);
        saveData(google, address, name, collectionName);
    }

    public void saveDataV2(String address, String name, String google, String collectionName) {
        address = preHandle(address);
        saveData(google, address, name, collectionName);
    }

    public void countDocuments(String collectionName) {
        System.out.println("Total places: " + mongoTemplate.count(new Query(), collectionName));
    }

    public void saveGoogleDataLink(String url, Function<String, String> handleAddress, String collectionName) {
        List<BaseEntity> baseEntities = crawlGoogleDataLink(url, handleAddress, collectionName);
        if (Objects.nonNull(baseEntities)) baseEntities.forEach(location -> {
            mongoTemplate.save(location, collectionName);
            log.info("Place: {}", gson.toJson(location));
        });
    }

    public static ChromeDriver getChromeDriver(String dirName) {
        String downloadFilepath = BASE_DIR + dirName;
        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadFilepath);
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--headless");
        ChromeDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        return chromeDriver;
    }
    public static ChromeDriver getChromeDriver() {
        return getChromeDriver(false);
    }

    public static ChromeDriver getChromeDriver(Boolean isHeadless) {
        ChromeOptions options = new ChromeOptions();
        if(isHeadless) {
            options.addArguments("--headless");
        }
        return new ChromeDriver(options);
    }

    public static String getKMLUrl(String googleDataLink, String dirName) {
        try {
            googleDataLink = googleDataLink.replace("embed", "viewer");
            String downloadFilepath = BASE_DIR + dirName;

            Path dir = Paths.get(downloadFilepath);
            Files.createDirectories(dir);
            FileUtils.cleanDirectory(new File(downloadFilepath));

            ChromeDriver chromeDriver = getChromeDriver(dirName);
            chromeDriver.get(googleDataLink);

            Utils.sleep(1000);
            chromeDriver.findElements(By.cssSelector("div[role='button']")).get(3).click();
            Utils.sleep(1000);
            chromeDriver.findElement(By.cssSelector("span[aria-label='Tải xuống KML']")).click();
            doRetryClick(() -> chromeDriver.findElements(By.cssSelector("div[data-cancelids] div[role='checkbox']")).forEach(WebElement::click));
            List<WebElement> buttons = chromeDriver.findElements(By.cssSelector("div[data-cancelids] div[role='button']"));
            buttons.get(buttons.size() - 1).click();
            Utils.sleep(1000);

            List<Path> files = Files.walk(dir, FileVisitOption.FOLLOW_LINKS).toList();
            String hrefHtml = Jsoup.parse(files.get(1).toFile()).getElementsByTag("href").first().html();
            return hrefHtml.split("CDATA\\[")[1].split("]]")[0];
        } catch (Exception e) {
            log.error("Error while get KML url: {}", e.getMessage(), e);
            return null;
        }

    }

    public static List<BaseEntity> crawlGoogleDataLink(String googleDataLink, Function<String, String> handleAddress, String collectionName) {
        String kmlUrl = getKMLUrl(googleDataLink, collectionName);
        List<BaseEntity> locations = new ArrayList<>();
        try {
            File kmlFile = new File("data.kml");

            assert kmlUrl != null;
            FileUtils.copyURLToFile(new URL(kmlUrl), kmlFile);
            Document doc = Jsoup.parse(kmlFile, "UTF-8", "");
            Elements placeMarks = doc.select("Placemark");
            for (Element placeMark : placeMarks) {
                String name = placeMark.select("name").text();
                String address = placeMark.select("description").text();
                String coordinates = placeMark.select("coordinates").text();
                double lat = Double.parseDouble(coordinates.split(",")[1]);
                double lon = Double.parseDouble(coordinates.split(",")[0]);

                if (Strings.isEmpty(address)) address = null;
                try {
                    address = handleAddress.apply(address);
                } catch (Exception ignored) {
                }
                BaseEntity location = BaseEntity.builder()
                        .name(name).address(address).lat(lat).lon(lon).build();
                locations.add(location);
            }
            return locations;
        } catch (Exception e) {
            log.error("Error while crawl gg link: {}", e.getMessage(), e);
        }
        return locations;
    }
    public void saveDataWithHref(String address, String name, String url, String collectionName) {
        saveDataWithHref(address, name , url, null, null, null, collectionName);
    }

    public void saveDataWithHref(String address, String name, String url, String id, String placeCode, Integer page, String collectionName) {
        if (Strings.isEmpty(url)) {
            saveData(address, name, collectionName);
            return;
        }
        List<Double> coordinates = Utils.handleCoordinatesByUrl(url);

        if (CollectionUtils.isEmpty(coordinates)) {
            String currentUrl = Utils.getUrl(driver, url);
            coordinates = Utils.getCoordinatesByUrl(driver, currentUrl);
        }

        if (!CollectionUtils.isEmpty(coordinates)) {
            double lat = coordinates.get(0);
            double lon = coordinates.get(1);

            BaseEntity entity = new BaseEntity();
            entity.setAddress(address);
            entity.setName(name);
            entity.setLat(lat);
            entity.setLon(lon);
            if(Objects.nonNull(id)) {
                entity.setId(id);
            }
            if(Objects.nonNull(placeCode)) {
                entity.setPlaceCode(placeCode);
            }
            if(Objects.nonNull(page)) {
                entity.setPage(page);
            }
            //mongoTemplate.save(entity, collectionName);
            log.info("Place: {}", gson.toJson(entity));
//            Utils.sleep(100);
        }
    }

    public void saveData(String googleAddress, String address, String name, String collectionName) {
        String currentUrl = Utils.getSearchUrl(driver, googleAddress, address);
        if(currentUrl == null) return;



        List<Double> coordinates = Utils.handleCoordinatesByUrl(currentUrl);
        if (!CollectionUtils.isEmpty(coordinates)) {
            double lat = coordinates.get(0);
            double lon = coordinates.get(1);

            BaseEntity entity = new BaseEntity();
            entity.setAddress(address);
            entity.setName(name);
            entity.setLat(lat);
            entity.setLon(lon);
            entity.setGoogle(true);
            mongoTemplate.save(entity, collectionName);
            i++;
            log.info("{} Place: {}",i, gson.toJson(entity));
        }
    }

    public void saveGoogleDataLinkSelenium(String googleDataLink, String collectionName) {
        googleDataLink = googleDataLink.replace("embed", "viewer");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        ChromeDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.get(googleDataLink);
        Utils.sleep(1000);
        try {
            chromeDriver.findElement(By.xpath("//*[@id=\"map-canvas\"]/div[2]/div[2]/div/div[1]")).click();
        } catch (Exception e) {
            log.warn("skip click expand left category");
        }
        chromeDriver.findElements(By.className("HzV7m-pbTTYe-KoToPc-ornU0b")).forEach(WebElement::click);

        int size = chromeDriver.findElements(By.cssSelector("div.HzV7m-pbTTYe-ibnC6b")).size();

        String oldUrl = "";
        for (int i = 0; i < size; i++) {
            int finalI = i;
            log.info("Handle element number: {}", finalI);
            doRetry(() -> chromeDriver.findElements(By.cssSelector("div.HzV7m-pbTTYe-ibnC6b")).get(finalI).click(), 3);
            String address = doRetry(() -> chromeDriver.findElement(By.xpath("//*[@id=\"featurecardPanel\"]/div/div/div[4]/div[2]/div[2]")).getText(), 3);

            if (Strings.isEmpty(address)) {
                address = doRetry(() -> chromeDriver.findElement(By.xpath("//*[@id=\"featurecardPanel\"]/div/div/div[4]/div[1]/div[2]/div[2]")).getText(), 3);
            }

            String name = doRetry(() -> chromeDriver.findElement(By.className("qqvbed-p83tee-lTBxed")).getText(), 3);
            if (Strings.isEmpty(address)) address = name;

            if (!Strings.isEmpty(address)) {
                String googleUrl = chromeDriver.getCurrentUrl();
                int count = 0;
                while (googleUrl.equals(oldUrl) && count < 10) {
                    googleUrl = chromeDriver.getCurrentUrl();
                    Utils.sleep(300);
                    count++;
                }
                oldUrl = googleUrl;
                saveDataWithHref(address, name, googleUrl, collectionName);
            }
            doRetry(() -> chromeDriver.findElements(By.className("HzV7m-tJHJj-LgbsSe-Bz112c")).get(6).click(), 3);
        }
    }

    public static String preHandleGG(String address) {
        return address
                .replaceAll("(?i)thành phố", ",")
                .replaceAll("(?i)phường", ",")
                .replaceAll("(?i)quận", ",")
                .replaceAll("(?i)huyện", ",")
                .replaceAll("(?i)thị xã", ",")
                .replaceAll(",\\s*,", ",");
    }

    public static String preHandle(String address) {
        return address
                .split(" 0\\d{2}")[0].split(" \\(0\\d{2}")[0].trim()
//                .replaceAll("(?i) đường", "")
                .replaceAll("(?i)TP\\.?\\s?", " thành phố ")
                .replaceAll(" Q\\.+", " quận ")
                .replaceAll(" Q\\s+", " quận ")
                .replaceAll(" P\\.+", " phường ")
                .replaceAll(",P\\.+", ", phường ")
                .replaceAll(" P\\s+", " phường ")
                .replaceAll(" H\\.+", " huyện ")
                .replaceAll(" H\\s+", " huyện ")
                .replaceAll("(?i)TW\\.?\\s?", " trung ương ")
                .replaceAll("(?i)TT\\.?\\s?", " thị trấn ")
                .replaceAll("(?i)TX\\.?\\s?", " thị xã ")
                .replaceAll("(?i)QL\\.?\\s?", " quốc lộ ")
                .replaceAll("X\\.+", " xã ")
                .replaceAll("\\s+", " ");
    }
}
