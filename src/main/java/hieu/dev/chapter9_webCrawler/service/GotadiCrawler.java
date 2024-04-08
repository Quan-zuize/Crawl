package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.DateTimeUtils;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.unbescape.html.HtmlEscape;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Service
@Slf4j
public class GotadiCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() {
        String domesticCities = BaseHttpClient.restTemplate.getForObject("https://api.gotadi.com/web/metasrv/api/airports/Citiespopular", String.class);
        gson.fromJson(domesticCities, JsonObject.class).getAsJsonArray("domesticCities")
                .asList().stream().map(JsonElement::getAsJsonObject)
                .map(province -> province.get("city2").getAsString())
                .forEach(province -> {
                    for (int page = 0; ; page++) {
                        String url = UriComponentsBuilder.fromHttpUrl("https://api.gotadi.com/web/api/v3/hotel/search-best-rates")
                                .queryParam("searchCode", province).queryParam("searchType", "AUTO")
                                .queryParam("pageNumber", page).queryParam("pageSize", 250)
                                .queryParam("checkIn", DateTimeUtils.convertTodayToString("yyyy-MM-dd"))
                                .queryParam("checkOut", DateTimeUtils.convertDayAfterToString(2, "yyyy-MM-dd"))
                                .queryParam("paxInfos", 2).queryParam("supplier", "EXPEDIA")
                                .queryParam("sortField", "order").queryParam("sortOrder", "DESC")
                                .queryParam("language", "vi").queryParam("currency", "VND")
                                .toUriString();
                        Request request = new Request.Builder()
                                .url(url)
                                .get()
                                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJHVUVTVCIsImF1dGgiOiJST0xFX1VTRVIsUk9MRV9CMkMiLCJYLWliZS1kIjoiNzg0ZmYwYjZkMzE2NzBmOTBlNDlkODk4ZTdhNGJjODk5ZTA4MTVhY2VjMzg5MzBiY2E4Mjg1NTI5YzA5M2I0ODM5NGMxNGJjYzQ1ZTg0ZDMzMzkwYjYyNmYzNjdiZWVmZDhiN2NmNDc5YzBlNDBmZDBmMTIzNDQyMDdkZjk0ZmM5MDc3ZTI4ODJmZTg1YjZmOTdmNDA2NDQ4MzQ3Mjg0MWE2OTczY2Q0ODRjNmIyMzVhYjA4ZGZjNDZhZDA4YTJhMDhiMWZjNWY5NDY5YmJmMWRmMWY4YjkzM2U5NTM5ZmU0YmQ5NzZiZjg0Yzc3YWEzZjAwNzRlNjE2NDlhYWRiMDM3YzhjZjUwODUxNDI2ZmNmN2E2Y2Q4YTFjMzJkMmJhOGE1MjcyMGVkNGE2Y2I3MTU5MjVjMWYwODFkNjNjZTM2YmJiMGE0MjM2NzQyYzYxNDE4MWFjZjdmYWUwYzg0N2M5OGU1OGJkNTEzNTI4YThhZGEzMDllMTlmNWI3Mjg0ZGU4MTBiYTk4YTJiMGZkYjI3MjY1MzdiMzZlNjBlODhlYzhlN2FlNWEyZGFjZWU2MjA4OGRlNDRkY2ViNjY2YmExMmQ1MjBlZWY5ZTFiZGVkNmJjNjQ1ZjE5ZjhiMWVjNTk1Zjc4ZmI5MjM2MmFkODMwN2ZiNTljZWUzODM3ZDE5YTBlNTIxMDMzMWU1YzQyMWNhYmEyMGIzZTA0MDJjODk0ZmY0NzRiMmFjYzQ3NmMxOTZkYzg0M2E1ZWI5OTEwNTU1OWEyOWM4ODE2ZmJkOWM3ZWVjODVkNjcyZWMzYjc0NjFmNjY3OTcyNmZkNTlkYzYxMzUyZDNjNTQwNDRmZmMzMmZkYjQ5NWQzYzdhOWE1MWU4ODI4NjBiYTNmNjBlNjVkNDMzMDIxMzk0M2U5ZTBkMjQzY2I4ZTZkMTU4Y2FiNWNkZjM1NTg1OTkzMDcyYmIyOGIyOTdiNzI1ZTkxNGUyNmUzMTg2ODcyODk3MjNjMWEwMWM1MWI5MDNjMjIzN2NjNDk2OWY5NGM5NmI2NmYwNjU5YTEyZjUzZTdlMWU4ZWQ0ZTM0Y2M4YzFhMGVhZWJjNjAzNzM5ZTFmZDA1MGQ0NzE0NmNkZDBkYzI3ZjY3OTRkZTcxODY0NjdlNTk3M2UzM2U2Mjk1ZDI0ODAxOTQ5MmI0YTA5ZTFiMjY3YzU5NjgwMGFhMzY3NDZiODYzOTQ1YmM2OTIxYWI1OTljNzkyM2UxN2I3NDEyOWRlYjU5YTY4ZWQ4YzQwODVjYzFlMGVmMjZjMzhkNDY2YTNhMWMwNDRjNmRhNDMzOWExNDQwZWM0MzNkM2FjNjcyMGEwOWNjMzU5MDMyZDNlZTY3OTViYTAyOWMwM2MwZTVhN2RiNzY1YjU3ZWI3NmYxNWUzYzdjYmE0OGY4MzQyOWM0NzhlZDhmNDYxYjg0MDExMDEzMTMwNTM5MTM3YzU0OTdmNmNkZjY4NGU5OGY0OGEyOWY2OWViM2MxYmIyNjczOTE1OGE4MjY1MThlNTE0YTkzYjQyM2ZhN2RkMDViZGFiMTZkZDZkNmJiN2U1ZWYxZTUzMzUwMDU5ZGY2ZTA4MGNjNTdjNTUyMzU0MDZjNzYxNjhmNjg5ZTFmNzQ0MWMxYzhmMWUwZDM5OWJkNWIxMTA1MGZkNDIyYmU2MjAzYmM1NmM5Y2NlNDY0MDI0ZjgwZWI4N2ZkNTQ1ZjNjNmRmM2IwNTEzYTBlNGZkOWVjNGRmMzU3OGEzOGY5OGQzNWQ2NzU1YjEyNzhhY2FlNDM2N2NkNjcyYmQyN2Q5NjhjNDU0NGMxZGQzNjc1YTNkYmQ1MGM0ZTYzNzUwNDI2ZmQ5ZjNlN2QxNjMxODQ2NGY5YzczZDhiNWE5MzU1YWRmNmU3ZGM2MGI0NjI5YmMxMTQ3NjNjM2VhNGRlNDFjMGFlZmJmYjYzZDhjZjdkYmQwOTNjY2VhYWI1MWYyMTRiOGIxMjU4MmU4ZmMyZDU5OTExOTRiYjI4MzdkNmU3ZmVhMDA5NWE4NDNhMjI2NGRiZjNlYTA5NDIzMTk4OGM4ZTM5ODU4YjcwZmNhMzQ0ZDZjZGNiOWJiNDYxNmJhYjU0ZmFkMTEwNmY0YjliZmIzYjgwYjMzZTMwYzdiMThhOGEwOGQ0Mzk1NWFhNjllZjIyOWZmMmZlOTM3NTIwOWNhZjBjZTY5M2Y5NGE4YzM0MGRjYjE2OGI0ODM4ZjNkZWNiYWViMTAyMzgzODgzZDdlOTM4ZDVlNjk3MWFlNGM2N2U5OTA5MGUzM2I0ZmVkZTk2ZmFiNTIwOGIwNzRmZmJmOWJkYzJkMGJmYTk5NjBlYmQ2ODBhNjMxMzA5N2Y2MTRlYTdlOTM2NWI4ZTQwMzM2MTBmNmFkODg0N2IzMjdmMGE0MmJhYWZkNmRkZWMyNzFkNTdhZjUxMmI1ZDFiZWYxNjMyNDU2OTc2MGVhY2M4MDJkYTg2YTdhY2FjNjM2NTQwM2Q2NTA0NDU1ZWMwNDZiNTlmNjNiMWE3MGFkMDY1Y2I2YTUyOGIxNGIxNmE5OThhMjI5MThjYjQxYzE0MGRmOGU4MzdkMDgxMzE0NmI0ODU0YzYzY2FmNjczY2FiNDZhNDgwMjk3YjFlZTAwMWFmMjVlNmM5ODkwYWQ4NDcwNjY5ZWU1ODZmMWM2NzhjNzkxZWY4OTczYTExMGNmMTBmNWRkMWZmMTk2ZTExYzNkMzA5Y2ExZTdmOTA3MDQ2ZWE3MDM1ZTNjNmJlMGRmMzllMmE3MDA4NGZmOTE3M2ViYmI5YTA0MzU4NzRjY2RkYjJlMGViNTliNDk0YjVhZGRkZjlmNjEwYzhmMjU4NWNjOWEwYjIwMzg0MmNiMGI3NGFmYTQ5ODg4YTQ5NTJhNWJkMTllNWZkODgwZWNhNWE0OWUwYmViMDE5OTc2MDljNzk3MjkwYzY2Zjk1N2FhMTI3YzU5OGFiZGQxZTU5Y2U1YTNhMTc5YjRjZmE4NjI4MTQ3NGZkZjlkZTc5MWRkMzllNzhkYmJlMWIyMGQwNGZlMDhmNWU0ZDgzODhiNDZlNGExODgwMThhYTEwZTVmNGI2YTk2ZjFiNzViY2VkYjgxNDA0NGFiMGUwOGNhZmZmYzg0Y2JiYjg4ZGZmMjhkZTBiMjdjMmQ1ZTkxMjU0ZGRhYzllMzUyOTkxMmM0Y2JmZTRlNzU0YWY1YjA3MzEwODQxMmY3YzJkYzU3Yjc5ODc5ZDUzNjY0YzBiMTMxNzRhMjNjNDQyMTE0ZTBmMThkMzU3NjE1OTJjZDI5M2I1Y2JiZjMxYmVhNzk1YmVlODgyZjgzOTM3MWIzZmVjZWZjNjBlYjQ0YjUyNjZmOWRlNjI0ZGQ3OWEwNTg0YWZiYzY3ZTY0NDVjOTE2MjczOWU3NmVlYjVjNjQwNjZlZTYwZmE4MTA0M2EyNzliZmNlZTBlZTBhMzA2MDI3YTU2YmM1ODY0YTE5YjU0MzA4ZjVjMTViZTFlZGQ4MjdjOGFkZjBiODZkMTZkZGU4MWFkOGI1OTEyMjYwMjhhMzU0ZDFlMzBiYTM0NDljYjU0YmYyZDg0YTEzMTZmMDhiMDFjYjQ0ZjM4N2M2ZDgyMTk1ZTFjMWZlMzUwNjg1ZjRmZmEzNDlkYmU4YWNjMWY4OWQ5MDFiMjFjODMxZjJiMzRiYTlkYzQ0YTU3NjU3ODk4MmJjZDk4ZTQwZDgxOWMzMzZjNjljMTMxMjhlMTQ0NDg5MThlZjI3NWZkYmFjMTVhZmY4Zjk5MmQ3ODNlYjMxY2VlMGJhZGYwZGYzNWUyYzg1NjM1Njc0OTM2ZDBhYzRkYzRkZDIxYTY3ZTBmMzBlYTYyMDcwZTEwNDNkZDIzN2VkMjkyOWM1ZmNiYmI4ZGE1MTcwZjA0OGQ4NzQ0NDVkYjc3YzM2MzMzODFiZDZjNTZmZTAyYTVlNjAxMDhlYTYyZmFkZDVhMWM0ZGZkNGI3Njg3MGI0YWNlMjk0ODM3NmIyNmJjZjJmZTE5NjEzZjQxNjQzNGQyOTY3NDJkY2Y4ZDk3OTA4N2YyZjljYmIzOTQxZjI5ZjRiMzA1MTU2YjQ1NWI3N2MzNTQzNDFlZDhlMTk0ODkzMTFiYWE1MjJkNTIyNTMyOTkyZjA4MDZlMDBhMmYxZWRjZjg3NDA1OWRlMzE0M2IzYzVjYTMzNzM4N2IwMGUwNzU1YmQ1NDMxZTJhYTFlMzdiNTE5MGE0NzUyMjM3ZDZhYmE5Zjg2NGNmZWFjZjFiODI3MDZlNGU4ODVmNDdjN2YyZGZkOGEzMDhkZjc0ZDkzNTFmNWE1MDU5ZDcxY2NiMzdmNDdhNDk3YTZkM2YxZDc0NTBhZDU1OTRlOGM5MzUyYzdkMzc0OThmMzY0NWIwMWY4NmM5ZDkxMDBhZGRlNjk0YTNhYTA3OWU0NmZhZmIxM2Q4MGU0NzczNGRmZGFiNDRmZmE3YWExZjdiMzQxOTZmMGU0YWMyYjk4NzdhMGQ2MWMxNjBiZGJjMWIwZGYzODUwZTQ5NzFiZjIxYTViNTA4NzE2MjAxODc0YmVmM2Q2ZDYwMmFjNThiMzliNWQ5M2U3MmJlMmQwYjU4MDliODM3NDhkYTMyMjhjYzFkOWE1NDRjMTc3NjI2NGQ5OTU3ODhjNTZlYjQyZjUzNDk1ODk4YTVhM2U5OWE5NjRiYmFhZGMzZDdlY2VkNCIsIlgtaWJlLWsiOiJhMTU0MmVlOTBjM2Y2YjhkIiwiZXhwIjoxNzExNTEyMzMzfQ.X0oEiRC0uNSFLTxARqujXErBadk8P64Xc_yLdgY7fUb-hEow0kfcoicvvSohpevg4jMPi8fAwMfHtdBF30ew0Q")
                                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                                .addHeader("Accept", "application/json;charset=UTF-8")
                                .addHeader("gtd-client-tracking-device-id", "296cc0d1-4853-41a0-9fc2-8ede80f83efb")
                                .addHeader("Cookie", "AWSALB=WmrxofWwE44gH4UPCtw3NjKrOSX75AZzq8jgXkosOGc2GB7on5hixfJfQZbi+GP3nzsZ7DIjWY3CMPhZn71M0CyCtm7TuJ8R3FnK/ejLPjHv7fH0wLSkJuP08b9j; AWSALBCORS=WmrxofWwE44gH4UPCtw3NjKrOSX75AZzq8jgXkosOGc2GB7on5hixfJfQZbi+GP3nzsZ7DIjWY3CMPhZn71M0CyCtm7TuJ8R3FnK/ejLPjHv7fH0wLSkJuP08b9j; NG_TRANSLATE_LANG_KEY=%22vi%22")
                                .build();
                        try (Response response = BaseHttpClient.okHttpClient.newCall(request).execute()){
                            String body = response.body().string();
                            JsonObject bodyObj = gson.fromJson(body, JsonObject.class).getAsJsonObject("result");
                            if(bodyObj.getAsJsonArray("propertyAvailable").isEmpty()) return;
                            int finalPage = page;
                            bodyObj.getAsJsonArray("propertyAvailable")
                                    .asList().stream().map(JsonElement::getAsJsonObject)
                                    .forEach(place -> {
                                        try {
                                            String id = place.get("propertyId").getAsString();
                                            String name = place.get("propertyName").getAsString();
                                            String address = getAddress(place.getAsJsonObject("address"));
                                            double lat = place.get("latitude").getAsDouble();
                                            double lon = place.get("longitude").getAsDouble();
                                            BaseEntity baseEntity = BaseEntity.builder()
                                                    .id(id).name(name).address(address).lat(lat).lon(lon).page(finalPage).placeCode(province).build();
                                            log.info("Place: {}", baseEntity);
                                            mongoTemplate.save(baseEntity, "gotadi_crawler");
                                        } catch (Exception e) {
                                            log.error("Error while handle place: {}", e.getMessage(), e);
                                        }
                                    });
                        } catch (Exception e) {
                            log.error("Error while handle page - province {} - {}: {}", page, province, e.getMessage(), e);
                        } finally {
                            Utils.sleepRandom(1000);
                        }

                    }
                });
    }

    private static String getAddress(JsonObject addressObject) {
        String lineOne = addressObject.get("lineOne").getAsString();
        String stateProvinceName = addressObject.get("stateProvinceName").getAsString();
        String city = addressObject.get("city").getAsString();
        if(Strings.isEmpty(lineOne)) lineOne = stateProvinceName;
        if(Strings.isEmpty(lineOne)) lineOne = city;
        if(Strings.isNotEmpty(lineOne)) {
            if(lineOne.endsWith(",")) lineOne = lineOne.trim().replaceAll(",$", "").trim();
        }
        return HtmlEscape.unescapeHtml(lineOne);
    }
}
