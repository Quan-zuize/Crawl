package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.CertUtils;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BankEntity;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Slf4j
@Service
public class AgribankCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        String url = "https://www.agribank.com.vn/wcm/connect/f19a8ca1-0507-486d-aa95-cb78b559b142/atm-chi-nhanh.js?MOD=AJPERES&attachment=true&id=1570853146584";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                .build();

        OkHttpClient okHttpClient = CertUtils.okHttpClientWithoutTrust();

        try (Response response = okHttpClient.newCall(request).execute()) {
            assert response.body() != null;
            String responseBody = response.body().string();
            responseBody = responseBody.replace("var arrBranch =", "{\"arrBranch\" :");
            responseBody = responseBody.substring(0, responseBody.length() - 1).concat("}");

            gson.fromJson(responseBody, JsonObject.class).get("arrBranch").getAsJsonArray()
                    .asList().stream().map(JsonElement::getAsJsonObject)
                    .forEach(b -> {
                        String title = b.get("title").getAsString();
                        String address = b.get("address").getAsString();
                        if (Objects.isNull(b.get("lat")) || Strings.isEmpty(b.get("lat").getAsString())) {
                            saveData(address, title, "agribank_branches");
                        } else {
                            double lat = b.get("lat").getAsDouble();
                            double lon = b.get("lng").getAsDouble();
                            String type = b.get("cn").getAsString().equals("cn") ? "branch" : "atm";

                            BankEntity bankEntity = BankEntity.builder()
                                    .name(title).address(address).lat(lat).lon(lon).type(type).build();
                            System.out.println(bankEntity);
                            mongoTemplate.save(bankEntity, "agribank_branches");
                        }
                    });
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
        }
    }
}
