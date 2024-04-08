package hieu.dev.chapter9_webCrawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

@Slf4j
public class Utils {
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Error while sleep: {}ms", millis, e);
        }
    }

    public static void sleepRandom(long millis) {
        long delta = new Random().nextLong(millis / 2);
        Utils.sleep(millis + delta);
    }

    public static <T> T doRetry(Supplier<T> supplier) {
        return doRetry(supplier, -1);
    }

    public static <T> T doRetry(Supplier<T> supplier, int times) {
        int count = 0;
        T response = null;
        do {
            if (count >= times && times > 0) break;
            try {
                response = supplier.get();
                if (count != 0) {
                    log.info("[{}] Retry .....", count);
                    Utils.sleep(500);
                }
            } catch (Exception e) {
                log.warn("[{}] No such element. Retry ...", count);
                Utils.sleep(500);
            } finally {
                count++;
            }
        } while (Objects.isNull(response) || (response instanceof String && Strings.isEmpty((CharSequence) response)));
        return response;
    }

    public static void doRetry(Runnable runnable) {
        doRetry(runnable, -1);
    }

    public static void doRetry(Runnable runnable, int times) {
        int count = 0;
        do {
            try {
                runnable.run();
                if (count != 0) {
                    log.info("[{}]Retry .....", count);
                }
                return;
            } catch (Exception e) {
                log.warn("[{}] No such element. Retry ...", count);
                Utils.sleep(500);
            } finally {
                count++;
            }
        } while (count < times || times <= 0);
    }

    public static void doRetryClick(Runnable runnable) {
        while (true) {
            try {
                Utils.sleep(100);
                runnable.run();
                break;
            } catch (Exception e) {
                log.warn("Retry ...");
                Utils.sleep(100);
            }
        }
    }


    public static String getSearchUrl(ChromeDriver driver, String address) {
        WebElement searchBoxElement = doRetry(() -> driver.findElement(By.id("searchboxinput")));
        searchBoxElement.clear();
        searchBoxElement.sendKeys(address);

        String oldUrl = driver.getCurrentUrl();
        WebElement searchElement = doRetry(() -> driver.findElement(By.id("searchbox-searchbutton")));
        searchElement.click();

        String currentUrl = driver.getCurrentUrl();
        int times = 0;
        while (currentUrl.equals(oldUrl) || !currentUrl.contains("/place/")) {
            if (times > 4) return currentUrl;
            try {
                if (driver.findElement(By.className("Q2vNVc")).getText().contains("Google Maps ")) {
                    log.error("Không tìm thấy địa chỉ: {}", currentUrl);
                    return null;
                }
            } catch (Exception ignored) {
            }
            currentUrl = driver.getCurrentUrl();
            times++;
            Utils.sleep(500);
        }
        return currentUrl;
    }

    public static String getSearchUrl(ChromeDriver driver, String addressWthNameBuilding, String address) {
        WebElement searchBoxDiv = doRetry(() -> driver.findElement(By.id("omnibox-singlebox")));
        WebElement searchBoxElement = doRetry(() -> driver.findElement(By.id("searchboxinput")));
        searchBoxElement.clear();

        searchBoxElement.sendKeys(addressWthNameBuilding);
        WebElement searchElement = doRetry(() -> driver.findElement(By.id("searchbox-searchbutton")));
        String oldUrl = driver.getCurrentUrl();

        String currentUrl = "";
        int i = 0;
        while (currentUrl.equals(oldUrl) || (!currentUrl.contains("/place/") && i < 2)) {
            searchElement.click();
            Utils.sleep(800);
            while (searchBoxDiv.getAttribute("class").contains("s4gWuf")) {
                Utils.sleep(200);
            }
            currentUrl = driver.getCurrentUrl();
            i++;
        }

        try {
            driver.findElement(By.cssSelector("div[aria-label][role='feed']"));
            return Utils.getSuggestAddressUrl(driver, addressWthNameBuilding, address);
        } catch (Exception ignored) {
            return currentUrl;
        }
    }

    public static String getAddress(ChromeDriver driver, String search) {
        WebElement searchBoxElement = doRetry(() -> driver.findElement(By.id("searchboxinput")));
        searchBoxElement.clear();
        searchBoxElement.sendKeys(search);

        WebElement searchElement = doRetry(() -> driver.findElement(By.id("searchbox-searchbutton")));
        searchElement.click();

        String address = doRetry(() -> driver.findElement(By.cssSelector("div[aria-label][data-section-id]")).getText());
        return address.substring(1).trim();
    }

    public static String getSuggestAddressUrl(ChromeDriver driver, String addressWthNameBuilding, String address) {
        WebElement searchBoxDiv = doRetry(() -> driver.findElement(By.id("searchbox")));
        WebElement searchBoxElement = driver.findElement(By.id("searchboxinput"));
        searchBoxElement.clear();
        searchBoxElement.sendKeys(addressWthNameBuilding);
        searchBoxElement.click();

        int maxRetries = 5;
        int retries = 0;
        WebElement suggestBoxElement = null;
        while (suggestBoxElement == null && retries < maxRetries) {
            try {
                suggestBoxElement = driver.findElement(By.id("ydp1wd-haAclf"));
            } catch (Exception e) {
                // Nếu không tìm thấy phần tử, tăng số lần retry và sleep trong một khoảng thời gian nhất định
                retries++;
                Utils.sleep(150);
            }
        }

        assert suggestBoxElement != null;
        List<WebElement> suggestions = suggestBoxElement.findElements(By.className("sW9vGe"));

        if (suggestions.size() == 1 && !suggestions.get(0).getText().contains("missing place")
                && !suggestions.get(0).getText().split("\n")[1].equals(addressWthNameBuilding)) {
            suggestions.get(0).click();
            Utils.sleep(800);
            while (searchBoxDiv.getAttribute("class").contains("s4gWuf")) {
                Utils.sleep(200);
            }
            searchBoxElement.clear();
            return driver.getCurrentUrl();
        }
        return Utils.getUrlByOnlyAddress(driver, address);
    }

    private static String getUrlByOnlyAddress(ChromeDriver driver, String address) {
        WebElement searchBoxDiv = doRetry(() -> driver.findElement(By.id("omnibox-singlebox")));
        WebElement searchBoxElement = doRetry(() -> driver.findElement(By.id("searchboxinput")));
        searchBoxElement.clear();
        searchBoxElement.sendKeys(address);

        WebElement searchElement = doRetry(() -> driver.findElement(By.id("searchbox-searchbutton")));
        String oldUrl = driver.getCurrentUrl();

        String currentUrl = "";
        int i = 0;
        while (currentUrl.equals(oldUrl) || (!currentUrl.contains("/place/") && i < 2)) {
            searchElement.click();
            Utils.sleep(1000);
            while (searchBoxDiv.getAttribute("class").contains("s4gWuf")) {
                Utils.sleep(200);
            }
            currentUrl = driver.getCurrentUrl();
            i++;
        }

        try {
            driver.findElement(By.cssSelector("div[aria-label][role='feed']"));
            log.info("Multiple results, ignore: {}, {}", address, currentUrl);
            return null;
        } catch (Exception ignored) {
            searchBoxElement.clear();
            return currentUrl;
        }
    }

    public static String getUrl(ChromeDriver driver, String oldUrl) {
//        driver.get(oldUrl);
//        String currentUrl = driver.getCurrentUrl();
//        int times = 0;
//        while (currentUrl.equals(oldUrl) || (!currentUrl.contains("/place/") && times < 4)) {
//            currentUrl = driver.getCurrentUrl();
//            if (!currentUrl.equals(oldUrl)) times++;
//            Utils.sleep(500);
//        }
//        return currentUrl;
        driver.get(oldUrl);
        String currentUrl = driver.getCurrentUrl();
        while (!currentUrl.contains("3d!") && !currentUrl.contains("&ll=") && !currentUrl.contains("/@") && !currentUrl.contains("/signin/")) {
            currentUrl = driver.getCurrentUrl();
            Utils.sleep(500);
            log.info("Retry ...");
        }
        return currentUrl;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }

    public static List<Double> getCoordinatesByUrl(ChromeDriver driver, String url) {
        try {
            List<Double> coordinates = handleCoordinatesByUrl(url);
            if (CollectionUtils.isEmpty(coordinates)) {
                driver.get(url);
                String currentUrl = driver.getCurrentUrl();
                coordinates = handleCoordinatesByUrl(currentUrl);
            }
            return coordinates;
        } catch (Exception e) {
            log.error("Error while get location");
            return null;
        }
    }

    public static List<Double> handleCoordinatesByUrl(String url) {
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        try {
            if (Strings.isEmpty(url)) return null;
            String[] params = url.split("!3d")[1].split("!4d");
            double lat = Double.parseDouble(params[0]);
            double lon = Double.parseDouble(params[1].split("!")[0]);
            return List.of(lat, lon);
        } catch (Exception e) {
            return handleCoordinatesByUrlV2(url);
        }
    }

    public static List<Double> handleCoordinatesByUrlV2(String url) {
        try {
            String[] params = url.split("@")[1].split(",");
            double lat = Double.parseDouble(params[0]);
            double lon = Double.parseDouble(params[1]);
            return List.of(lat, lon);
        } catch (Exception e) {
            return handleCoordinatesByUrlV3(url);
        }
    }

    public static List<Double> handleCoordinatesByUrlV3(String url) {
        try {
            String[] params = url.split("!2d")[1].split("!3d");
            double lon = Double.parseDouble(params[0]);
            double lat = Double.parseDouble(params[1].split("!")[0]);
            return List.of(lat, lon);
        } catch (Exception e) {
            return handleCoordinatesByUrlV4(url);
        }
    }

    public static List<Double> handleCoordinatesByUrlV4(String url) {
        try {
            String[] coordinates = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().toSingleValueMap().get("sll").split(",");
            return List.of(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        } catch (Exception e) {
            return handleCoordinatesByUrlV5(url);
        }

    }

    public static List<Double> handleCoordinatesByUrlV5(String url) {
        try {
            Map<String, String> params = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().toSingleValueMap();
            String coordinateStr = params.get("query");
            String[] coordinate = coordinateStr.split(",");
            return List.of(Double.parseDouble(coordinate[0].trim()), Double.parseDouble(coordinate[1].trim()));
        } catch (Exception e) {
            return handleCoordinatesByUrlV6(url);
        }
    }

    public static List<Double> handleCoordinatesByUrlV6(String url) {
        try {
            Map<String, String> params = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().toSingleValueMap();
            String coordinateStr = params.get("ll");
            String[] coordinate = coordinateStr.split(",");
            return List.of(Double.parseDouble(coordinate[0].trim()), Double.parseDouble(coordinate[1].trim()));
        } catch (Exception e) {
            return handleCoordinatesByUrlV7(url);
        }
    }

    public static List<Double> handleCoordinatesByUrlV7(String url) {
        try {
            Map<String, String> params = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().toSingleValueMap();
            String coordinateStr = params.get("q");
            String[] coordinate = coordinateStr.split(",");
            return List.of(Double.parseDouble(coordinate[0].trim()), Double.parseDouble(coordinate[1].trim()));
        } catch (Exception e) {
            log.info("Current url not contain data {}", url);
            return null;
        }
    }

    public static String normalize(String input) {
        return StringEscapeUtils.unescapeJava(input);
    }
}
