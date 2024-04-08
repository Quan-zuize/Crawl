package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.client.BaseHttpClient.okHttpClient;

@Service
@Slf4j
public class HcCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        List<String> provinceIds = getProvinceIds();
        System.out.println(provinceIds);

        ChromeDriver chromeDriver = new ChromeDriver();

        provinceIds.forEach(provinceId -> {
            String url = UriComponentsBuilder.fromHttpUrl("https://hc.com.vn/ords/wwv_flow.ajax")
                    .queryParam("p_flow_id", 134)
                    .queryParam("p_flow_step_id", 23)
                    .queryParam("p_instance", 24695692000468L)
                    .queryParam("p_request", "PLUGIN=3fCxYvS-DcKZqNP3QUjPcqlkmkFSEjNpuhGMBJT-GRY")
                    .queryParam("p_widget_action", "reset")
                    .queryParam("x01", "28514403508785102")
                    .queryParam("p_json", String.format("{\"pageItems\":{\"itemsToSubmit\":[{\"n\":\"P23_PROVINCE\",\"v\":\"%s\",\"ck\":\"QIJKVMBhlA7wrAtVW0EQwJdUlCwqCEuoU6GuhxWmW76JEmuu5rvUNairL3qqACfb0BrxqV2EhSqHSllP3hV9ww\"}],\"protected\":\"fdFBQjNxn18jA9nF2hIxbuGAZelbV_pu7C9VvwuDyunFI6Lou8pG3jJS0h-SFtVuQazGjczd3DUqlBs6pba9XrwMyxx-fS_exkTHBB58lYmRMLTIHfTTs2uBlz93mUbUNyOVEY4sFLgrJzamI9Uy0h5qGSX6S3I7uabR1jm-xB0\",\"rowVersion\":\"\"},\"salt\":\"108565127749470967255775594496452837179\"}", provinceId))
                    .toUriString();

            HttpHeaders httpHeaders = new HttpHeaders(BaseHttpClient.headers);
            httpHeaders.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpHeaders.set("Cookie", "ECOM_CLIENT_ID=208720808; P0_IS_MOBILE=NO; HC_ECOMMMERCE=ORA_WWV-9DXci508QKT_IWQrer7xbH1g; _gid=GA1.3.203914793.1710239117; viteexConfig=%7B%22app_id%22%3A%22BnZenKl4jJ%22%2C%22app_domain%22%3A%22https%3A//dienmayhc.net/%2Chttps%3A//hc.mepuzz.com%2Chttps%3A//hc.com.vn/%22%2C%22app_status%22%3A10%2C%22public_key%22%3A%22BFx_ER3V0xQrBWpe5JAiW0syNbE6spGnw8uv_vOlTkK8dE0w7sjUC2wrWnkAxcbxXoN-eim8feYq3ItEAeVgSl0%22%2C%22not_ask_allow_in_day%22%3A0%2C%22alwaysSubcribe%22%3A0%2C%22is_track_reload_url%22%3A0%2C%22max_receive%22%3A0%2C%22notif_welcome%22%3A%5B%5D%7D; __zi=3000.SSZzejyD4DrbYVgitnWHacVTvEBH05BETTVxy9CIHDaqYBttb5q9tpYFlFlGMG_USegvyDPS2jmsCG.1; mp_sid=1710294419939.4167; _gat_gtag_UA_151174138_1=1; _gcl_au=1.1.2121205588.1710294793; _ga=GA1.1.1921979467.1710239117; MEPUZZ_VIEW_N_PAGE=5; _ga_RNELLY32L5=GS1.1.1710294419.2.1.1710294821.31.0.0");

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), ""))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .headers(Headers.of(httpHeaders.toSingleValueMap()))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute();){
                String body = Objects.requireNonNull(response.body()).string();
                Document document = Jsoup.parse(body);
                document.select(".hc_storelist__map").forEach(linkStoreElement -> {
                    String storeUrl = "https://hc.com.vn/ords/" + linkStoreElement.attr("href");
                    System.out.println(storeUrl);
                try {
                    chromeDriver.get(storeUrl);
                    String name = chromeDriver.findElement(By.cssSelector(".welcome-about-us strong")).getText();
                    String address = chromeDriver.findElements(By.cssSelector(".welcome-about-us > div > p")).stream().map(WebElement::getText).filter(addressText -> addressText.contains("Địa chỉ: ")).findAny().orElse(null);
                    String googleUrl = chromeDriver.findElement(By.cssSelector(".welcome-about-us iframe[src]")).getAttribute("src");
                    saveDataWithHref(address, name, googleUrl, "hc_places");
                } catch (Exception e) {
                    log.error("Error while get link: {}", storeUrl, e);
                } finally {
                    Utils.sleep(200);
                }
                });
            } catch (IOException e) {
                log.error("Error {}", url);
            }


        });
    }

    private static List<String> getProvinceIds() throws IOException {
        Document document = Jsoup.connect("https://hc.com.vn/ords/br--he-thong-sieu-thi-dien-may-hc").get();
        List<String> provinceIds = new ArrayList<>(document.select(".hc_select_city > option").stream().map(Element::val).toList());
        provinceIds.remove(0);
        return provinceIds;
    }
}
