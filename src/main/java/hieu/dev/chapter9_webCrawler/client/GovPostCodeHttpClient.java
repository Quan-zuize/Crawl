package hieu.dev.chapter9_webCrawler.client;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.dto.GovSearchResponse;
import hieu.dev.chapter9_webCrawler.entity.GovPlacePostCodeEntity;
import hieu.dev.chapter9_webCrawler.model.PostCode;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
public class GovPostCodeHttpClient extends BaseHttpClient {
    private static final HttpHeaders headers = new HttpHeaders();
    private static final long sleepMillis = 300;

    static {
        headers.add("User-Agent", "Dart/3.1 (dart:io)");
    }

    public static void main(String[] args) throws Exception {
        getPostCodes("10-14");
        testApiLocationById();
        testApiLocation();
        crawlPostCode("10");
    }

    public static void testApiLocation() {
//        String ndasCode = "1151502853";
//        String addressId = callApiAddressId(ndasCode);
//        callApiLocationData(ndasCode, addressId);
    }

    private static void testApiLocationById() {
//        GovSearchResponse govSearchResponse = callApiSearchAndDecrypt("112070000");
//        GovPlacePostCodeEntity response = callApiPlaceAndDecrypt("vmap:ADDRESS:3d270c46-3801-484d-9553-39ff270dcbf5", "112070000");
    }

    public static GovSearchResponse callApiSearchAndDecrypt(String idPrefix) {
        try {
            String searchUri = UriComponentsBuilder.fromHttpUrl("https://api.diachiso.gov.vn/vpost/geocoding/search")
                    .queryParam("text", idPrefix)
                    .build(true).toUriString();
            GovSearchResponse govSearchResponse = callApiAndDecrypt(searchUri, headers, GovSearchResponse.class);
            log.info("Response data: {}-{}", idPrefix, gson.toJson(govSearchResponse));
            return govSearchResponse;
        } catch (Exception e) {
            log.error("Error while get place data id {}: {}", idPrefix, e.getMessage());
            return null;
        } finally {
            Utils.sleep(sleepMillis);
        }

    }

    public static GovPlacePostCodeEntity callApiPlaceAndDecrypt(String placeId, String idPrefix) {
        try {
            String placeUri = UriComponentsBuilder.fromHttpUrl("https://api.diachiso.gov.vn/vpost/geocoding/place")
                    .queryParam("placeId", placeId)
                    .build(true).toUriString();

            GovPlacePostCodeEntity govPlaceResponse = callApiAndDecrypt(placeUri, headers, GovPlacePostCodeEntity.class);
            govPlaceResponse.setIdPrefix(idPrefix);
            log.info("Response data: {}-{}", idPrefix, gson.toJson(govPlaceResponse));
            return govPlaceResponse;
        } catch (Exception e) {
            log.error("Error while get place data id {}: {}", idPrefix, e.getMessage());
            return null;
        } finally {
            Utils.sleep(sleepMillis);
        }
    }

    public static String callApiAddressId(String ndasCode) {
        try {
            URI addressIdUri = UriComponentsBuilder.fromHttpUrl("https://api.diachiso.gov.vn/vpost/v2/address/get-addressid-by-ndasCode")
                    .queryParam("ndasCode", ndasCode)
                    .build(true).toUri();

            HttpEntity<String> entity = new HttpEntity<>("body", headers);
            ResponseEntity<String> forEntity = restTemplate.exchange(addressIdUri, HttpMethod.GET, entity, String.class);
            String responseData = forEntity.getBody();
            log.info("Response from get address id: {}", responseData);
            return gson.fromJson(responseData, JsonObject.class).get("data").getAsString();
        } catch (Exception e) {
            log.error("Error while get place data ndasCode {}: {}", ndasCode, e.getMessage());
            return null;
        } finally {
            Utils.sleep(sleepMillis);
        }
    }

    public static void callApiLocationData(String ndasCode, String addressId) {
        URI locationUri = UriComponentsBuilder.fromHttpUrl("https://api.diachiso.gov.vn/vpost/number-plate/" + addressId + "/" + ndasCode)
                .build(true).toUri();

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> locationEntity = restTemplate.exchange(locationUri, HttpMethod.GET, entity, String.class);
        String body = locationEntity.getBody();
        log.info("Response from get location data: {}", body);
    }

    public static List<String> getPostCodes(String lv1Postcode) {
        String[] params = lv1Postcode.split("-");
        int start = Integer.parseInt(params[0]);
        int end = Integer.parseInt(params[0]);
        if (params.length == 2) {
            end = Integer.parseInt(params[1]);
        }

        List<String> postCodes = LongStream.rangeClosed(start, end).flatMap(
                postCodePrefix -> IntStream.rangeClosed(0, 999).boxed()
                        .map(postCodePostfix -> String.format("%03d", postCodePostfix))
                        .map(postCodePostfix -> postCodePrefix + postCodePostfix)
                        .mapToLong(Long::valueOf)
        ).boxed().map(String::valueOf).toList();
        log.info("Generate postcode ha noi size: {}", postCodes.size());
        return postCodes;
    }

    public static List<String> getListIdPrefix(String postCode) {
        return IntStream.rangeClosed(0, 9999).boxed()
                .map(ndasIdPrefix -> String.format("%04d", ndasIdPrefix))
                .map(ndasIdPrefix -> postCode + ndasIdPrefix)
                .map(String::valueOf).toList();
    }


    public static void crawlPostCode(String lv1PostCode) {
        PostCodeMetaData metaData = new PostCodeMetaData(lv1PostCode);
        while (true) {
            Document document = callApiPostCodes(lv1PostCode, metaData);
            if (Objects.isNull(document)) return;
            List<PostCode> postCodes = document.select("h4").stream().map(PostCode::from).collect(Collectors.toList());
            log.info("Get postcode page {} size {}, {}", metaData.getBtnPaging(), postCodes.size(), gson.toJson(postCodes));

            metaData.setState(document);
            metaData.nextPage();
        }
    }

    private static Document callApiPostCodes(String lv1PostCode, PostCodeMetaData metaData) {
        log.info("Handling {} page {}", lv1PostCode, metaData.btnPaging);

        String url = "https://mabuuchinh.vn/default.aspx?page=SearchMBC";
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        String cookieValue = String.format("MBC=text_search=%s&type_search=0", lv1PostCode);
        httpHeaders.add("cookie", cookieValue);

        ResponseEntity<String> response;
        if (metaData.getBtnPaging() == 1) {
            HttpEntity<String> entity = new HttpEntity<>("", httpHeaders);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } else {
            httpHeaders.add("Content-Type", "multipart/form-data");
            MultiValueMap<String, String> formBody = metaData.toMultiMap();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formBody, httpHeaders);
            response = restTemplate.postForEntity(url, entity, String.class);
        }
        String body = org.unbescape.html.HtmlEscape.unescapeHtml(response.getBody());
        if (Objects.isNull(body)) return null;
        return Jsoup.parse(body);
    }

    @Data
    public static class PostCodeMetaData {
        private String lv1PostCode;
        private String viewState;
        private String viewStateGenerator;
        private String eventValidation;
        private Integer currentPage;
        private Integer btnPaging;

        public PostCodeMetaData(String lv1PostCode) {
            this.lv1PostCode = lv1PostCode;
            this.btnPaging = 1;
        }

        public void setState(Document document) {
            viewState = document.select("input[name='__VIEWSTATE']").val();
            viewStateGenerator = document.select("input[name='__VIEWSTATEGENERATOR']").val();
            eventValidation = document.select("input[name='__EVENTVALIDATION']").val();
        }

        public void nextPage() {
            if (btnPaging != 1) currentPage = btnPaging;
            btnPaging += 1;
        }

        public MultiValueMap<String, String> toMultiMap() {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("__VIEWSTATE", viewState);
            multiValueMap.add("__VIEWSTATEGENERATOR", viewStateGenerator);
            multiValueMap.add("___EVENTVALIDATION", eventValidation);
            if (Objects.nonNull(currentPage)) {
                multiValueMap.add("ctl00$ctl06$hdf_CurrentPaging", String.valueOf(currentPage));
            }
            multiValueMap.add("ctl00$ctl06$rptbtn_phantrang$ctl01$btn_paging", String.valueOf(btnPaging));
            multiValueMap.add("ctl00$ctl06$rptbtn_phantrang$ctl02$btn_paging", String.valueOf(btnPaging));
            multiValueMap.add("ctl00$ctl06$rptbtn_phantrang$ctl03$btn_paging", String.valueOf(btnPaging));
            multiValueMap.add("ctl00$ctl06$rptbtn_phantrang$ctl04$btn_paging", String.valueOf(btnPaging));
            multiValueMap.add("ctl00$ctl06$rptbtn_phantrang$ctl05$btn_paging", String.valueOf(btnPaging));
            multiValueMap.add("ctl00$ctl06$rptbtn_phantrang$ctl06$btn_paging", String.valueOf(btnPaging));
            multiValueMap.add("ctl00$ctl06$hdf_type", "0");
            multiValueMap.add("ctl00$ctl06$hdf_id", lv1PostCode);
            multiValueMap.add("ctl00$ctl06$txtMBC", lv1PostCode);

            return multiValueMap;
        }
    }
}
