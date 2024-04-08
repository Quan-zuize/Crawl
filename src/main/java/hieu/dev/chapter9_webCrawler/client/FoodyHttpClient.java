package hieu.dev.chapter9_webCrawler.client;

import hieu.dev.chapter9_webCrawler.dto.FoodyCityResponse;
import hieu.dev.chapter9_webCrawler.dto.FoodyLocationResponse;
import hieu.dev.chapter9_webCrawler.entity.FoodyLocationEntity;
import hieu.dev.chapter9_webCrawler.model.FoodyCity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class FoodyHttpClient extends BaseHttpClient {
    public static void main(String[] args) {
        List<FoodyCity> cities = getListCity();
        List<String> categories = getListCategories();
        System.out.println(getLocations(166));
        System.out.println(getLocations("ha-noi/beauty/tiem-nail", 50));
    }
    public static List<String> getListCategories() {
        String url = "https://www.foody.vn/common/_TopCategoryGroupMenu?isUseForSearch=true";
        try {
            Connection connect = Jsoup.connect(url);
            Document document = connect.get();
            List<String> result = document.select("a[href]").stream()
                    .map(element -> element.attr("href"))
                    .filter(uri -> !uri.contains("dia-diem"))
                    .map(uri -> uri.split("[/?]"))
                    .filter(uriElements -> uriElements.length > 3)
                    .map(uriElements -> uriElements[2] + "/" + uriElements[3])
                    .collect(Collectors.toList());
            log.info("Get list categories size {}, {}", result.size(), gson.toJson(result));
            return result;
        } catch (Exception e) {
            log.error("Error while get page url {}: {}", url, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    public static List<FoodyCity> getListCity() {
        String url = "https://www.foody.vn/__get/Common/GetPopupLocation";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Requested-With", "XMLHttpRequest");

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            String data = responseEntity.getBody();
            FoodyCityResponse foodyCityResponse = gson.fromJson(data, FoodyCityResponse.class);
            List<FoodyCity> result = foodyCityResponse.getAllLocations();
            log.info("Foody city response size {}: {}", Objects.nonNull(result) ? result.size() : 0, gson.toJson(result));
            return result;
        } else {
            log.error("Error while call api: {}", responseEntity.getStatusCode());
        }

        return new ArrayList<>();
    }


    public static List<FoodyLocationEntity> getLocations(int page) {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.foody.vn/__get/Place/HomeListPlace")
                .queryParam("t", System.currentTimeMillis())
                .queryParam("page", page)
                .queryParam("count", 12)
                .queryParam("type", 1)
                .build(true).toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Requested-With", "XMLHttpRequest");

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            String data = responseEntity.getBody();
            FoodyLocationResponse foodyLocationResponse = gson.fromJson(data, FoodyLocationResponse.class);
            List<FoodyLocationEntity> result = foodyLocationResponse.getItems();
            log.info("Foody location response size {}: {}", Objects.nonNull(result) ? result.size() : 0, gson.toJson(result));
            return result;
        } else {
            log.error("Error while call api: {}", responseEntity.getStatusCode());
        }

        return new ArrayList<>();
    }

    public static List<FoodyLocationEntity> getLocations(String category, int page) {
        String cookie = "bc-jcb=1; flg=vn; fd.res.view.217=146023; _ga=GA1.2.1307915008.1706776245; fbm_395614663835338=base_domain=.foody.vn; __utmz=257500956.1708485867.2.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); fd.keys=; __ondemand_sessionid=jmhik112bffnjtjqmahd3gbi; _gid=GA1.2.2114976700.1708913906; __utmc=257500956; floc=218; FOODY.AUTH.UDID=200c96b4-826c-4957-a805-9a106169b3e6; fd.verify.password.17456197=26/02/2024; ilg=0; FOODY.AUTH=F696CD30687412F62035F455D600BE396EE2CB56262E98DCE9A599FDC76A4E90C5793FE83C7B83EA3DA6ED2AC0B1E6B4F5EFEC549F4F15368470091268494A974128619325C4068811C599B2B19F0E5B91C94F8A2D250F014ED76AE140DB3DD4F9B10CBD840EC988BD5ECA25FA205EE755AC46BFF4E0BA8C4DA7472AF96E07EC82407EEA8E870BDEC5A29DC31953662FD3B913D23F158B273E94A8BB7090BE05EBB3A0E4565088794A7B86C700E4A86BBB89E39BB92907E84ED82CB54EFC0434573B029115E9D82BC08DCEFF9729FF0174A7A45535025BC2A555A4B9BEACE2825B10F283226B01C09F2A00A6A7A4CD3E; fd.res.view.218=187168,97578,629179,1186426; __utma=257500956.1307915008.1706776245.1708918153.1708928252.5; fbsr_395614663835338=mZjUY5f2BnUOKjhK92EBDucs2873HWfDawaWWOxt_mo.eyJ1c2VyX2lkIjoiMTExMDYxODgxOTEzNTM1NyIsImNvZGUiOiJBUUNzRDN3N2xVWV8xT1JFRFdxTWlHRnVTYXZtQm9uYmprczVKX0tXa29iLW5iQWFXMi04VDY4ODYtNktJODN0STRVV3Vuc0pDWXctUUlsSXdHdmU0cVlJVDI1Q0RHcHM2LTVLcVhZYTdVUWtrUmk0V0F4Zy1Tb2V2UkY0Vi0yWjN5ZXFJNEZ0MHR2NHFGNHNhZkdObTBVRmFQa2dHWG9CaG9ZaGVTWFk3VzlQOHE5Tkc2VTVxQ092Um1rZ1ZjSFdfQk1MY1VMZUk5WFQxMnk3NlN6TGVZQk00alRuTWwzMEFDVUNBVUVHeklXRjNkWWpDazN0ako3T3pzaWdlbnlKXzVxRGNYSnJxdE45OHdZTjlYbUdqT3hoRmszYjNUbzJ2X2RvZy1nWG1qam1ybUhENFViR1p5aU0xaGtsVkYyRU5ZajNyT3RxMmVrZlVSVTRGd1lVdmJwcSIsIm9hdXRoX3Rva2VuIjoiRUFBRm56emVCZnNvQk84MWFQQzh3WXBnR1RqRklsYXNwRXE0Z3czQlI0ODU2MVpCWHVqZUhhS1pDWkF6N0pzSmVzcTQ2V1lxVTVDM3N2QkkwSXhGWFliQjBRb1RPYW9UYU41N2FvZHV0cGRXZG5PdVpDWXJ6bVRGOXd5NTJZbmptUERKWkJnTXd0WkIwbndwQ0pnYmlSWkNQOGtnQ2dGZWdHZ3pVNzJMVTBjMzlPWkMwdjZ4MXlxWkNoWkJhdmJ0UVpEWkQiLCJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImlzc3VlZF9hdCI6MTcwODkyOTg0NH0; fbsr_395614663835338=4fqQRsSAKupiVuRKki2SQ0o9xj_kL5BKW31D3NS0Bkc.eyJ1c2VyX2lkIjoiMTExMDYxODgxOTEzNTM1NyIsImNvZGUiOiJBUUFFazEyTkpFbS1YMmdRTzNiNFVQSXpqUVhIQkJQbkR5VjFNOFp4QWxmSncwQVhNNFdmdm1oaXhJSmt5MHA2RC1Bdk9QemlsakdiZVZMSzBTRlhsQ1B5TDhpNHhFZkEtTFFzZjloUkt2TU0yVW5MQlBPVnRZNWxxLWg3TF9xOHVuV1FDNFNzZzVsOEZtUXRUQjR1YkFfbzdCNGJPc3BfUWpBUnJmU3FZSmJvVkRyYU5Sa0tsakRKbC00SWxxSG1nMWJlaVUzVE94TGZxWm1FM05zLWpUWm5Gc2VnSXJNMFUyc1BDTHV6SFVwbDh0QUVqU2tyeFZHeTUzTURYak5KSkJQN0h2d19HdHVpSlZjLTR0M196bGYyTnNQV2xRVGxDQXdxd0g5V0JuUmcxaHVhYm1VS2o0dWM0ZjJaZFA3MGZaeFBlOHgxd3JjS0drc19aOUpyZjBfRyIsIm9hdXRoX3Rva2VuIjoiRUFBRm56emVCZnNvQk84MWFQQzh3WXBnR1RqRklsYXNwRXE0Z3czQlI0ODU2MVpCWHVqZUhhS1pDWkF6N0pzSmVzcTQ2V1lxVTVDM3N2QkkwSXhGWFliQjBRb1RPYW9UYU41N2FvZHV0cGRXZG5PdVpDWXJ6bVRGOXd5NTJZbmptUERKWkJnTXd0WkIwbndwQ0pnYmlSWkNQOGtnQ2dGZWdHZ3pVNzJMVTBjMzlPWkMwdjZ4MXlxWkNoWkJhdmJ0UVpEWkQiLCJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImlzc3VlZF9hdCI6MTcwODkzMDg0MH0; gcat=service; __utmt_UA-33292184-1=1; __utmb=257500956.18.10.1708928252; _ga_6M8E625L9H=GS1.2.1708928251.5.1.1708932411.31.0.0";
        String url = UriComponentsBuilder.fromHttpUrl("https://www.foody.vn/" + category)
                .queryParam("ds", "Restaurant")
                .queryParam("vt", "row")
                .queryParam("st", 1)
                .queryParam("page", page)
                .queryParam("append", true)
                .build(true).toUriString();

//        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("Cookie", cookie);

        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            String data = responseEntity.getBody();
            FoodyLocationResponse foodyLocationResponse = gson.fromJson(data, FoodyLocationResponse.class);
            List<FoodyLocationEntity> result = foodyLocationResponse.getSearchItems();
            log.info("Foody location response {}-{} size {}: {}", category, page, Objects.nonNull(result) ? result.size() : 0, gson.toJson(result));
            return result;
        } else {
            log.error("Error while call api: {}", responseEntity.getStatusCode());
        }

        return new ArrayList<>();
    }
}
