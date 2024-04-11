package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.CertUtils;
import hieu.dev.chapter9_webCrawler.entity.BankEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hieu.dev.chapter9_webCrawler.Utils.gson;

@Slf4j
@Service
public class AgribankCrawler extends BaseSeleniumCrawler {
    List<BankEntity> banks = new LinkedList<>();
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
                        title = (title.contains("Agribank") || title.contains("agribank"))
                                ? title : "Agribank " + title;
                        String address = b.get("address").getAsString();
                        if(!address.contains("Số")){
                            return;
                        }
                        String type = b.get("cn").getAsString().equals("cn") ? "branch" : "atm";
                        Optional<BankEntity> existData = banks.stream().filter(d -> d.getAddress().equals(address)).findFirst();
                        if(existData.isPresent()){
                            BankEntity updateBank = existData.get();
                            updateBank.setName(title);
                            return;
                        }

                        if (Objects.isNull(b.get("lat")) || Strings.isEmpty(b.get("lat").getAsString())) {
                            Query query = new Query();
                            query.addCriteria(Criteria.where("name").is(title));
                            if(mongoTemplate.findOne(query, BankEntity.class, "agribanks") != null){
//                                Update update = new Update().set("name", title);
//                                mongoTemplate.updateFirst(query, update, "agribanks");
                                return;
                            }
                            saveDataV2(address, title, preHandleGGBank(title, address), "agribanks");
                        } else {
                            double lat = b.get("lat").getAsDouble();
                            double lon = b.get("lng").getAsDouble();

                            BankEntity bankEntity = BankEntity.builder()
                                    .name(title).address(address).lat(lat).lon(lon).type(type).build();
                            //System.out.println(bankEntity);
                            banks.add(bankEntity);
                        }
                    });
        } catch (Exception e ) {
            log.error("Error: {}", e.getMessage(), e);
        }

//        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BankEntity.class, "agribanks");
//        banks.forEach(bankEntity -> {
//            Update update = new Update().set("name", bankEntity.getName());
//            Query query = new Query();
//            query.addCriteria(Criteria.where("address").is(bankEntity.getAddress()));
//
//            bulkOperations.updateOne(query, update);
//        });
//        bulkOperations.execute();
        log.info("Insert successfully");
    }

    private String preHandleGGBank(String title, String address){
        if(address.startsWith("Số")){
            return "Agribank ".concat(address.split("Số ")[1]
                    .split("(?i),\\s*Tỉnh")[0].replace("nhà ",""));
        }
        return title.replaceAll("(?i) Huyện", "")
                .replaceAll("(?i) Thành phố", "");
    }
}
