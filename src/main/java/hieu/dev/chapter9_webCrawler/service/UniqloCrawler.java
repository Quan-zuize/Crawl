package hieu.dev.chapter9_webCrawler.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.entity.BaseEntity;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UniqloCrawler extends BaseHttpClient {
    @Autowired
    MongoTemplate mongoTemplate;

    public void crawl() {
        for (int page = 0; ; page++) {
            String url = UriComponentsBuilder.fromHttpUrl("https://map.uniqlo.com/vn/api/storelocator/v1/vi/stores")
                    .queryParam("limit", 100)
                    .queryParam("offset", page)
                    .queryParam("lang", "local")
                    .queryParam("RESET", true)
                    .queryParam("r", "storelocator").toUriString();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .addHeader("Cookie", "_abck=F924201CB74895BB20788092AB0C4C05~-1~YAAQbFnKF/nPczWOAQAA8rmOUAuZ5CaV5HXpC5dpVNnLgHALx/jbB6atC3ThdWNOxP5hzfWsInoNQYvLHUWQxjSF4ioUms9xzgN2JLdYqaSczVV2wJM1KtQ3c/wOmzF2EAcUbYs744i5/5cXJmcjXK1vTA7xhaAVKNDHu6lyaz9jlVuz7q29IZwDjmLTKd6OJMrD187GJu/lpw2TY129TAEfREOw8+3rTNZyHDMTuVIKNlinhfhCZGV9ClEDWxiHbgJ2dYO5hMSdRiK/u3SnqHPiDIx9P7bEXwCkvJ3KFyXxZsap1qL/koUZ6oNHOvcV9a+B08+xqOA2/txS6QLGDKcFQ7rGFH5y1bKXZOFbEDXCfbJdovZ3QGrqlQ==~-1~-1~-1; ak_bmsc=B6785931AE255D8D0AB09FF21CE81AA7~000000000000000000000000000000~YAAQbFnKF/rPczWOAQAA87mOUBcsfF844yeF0Ie3OZnIXF4zUAiMmrC98qHjaG+BdEaDYxJ36ceWe4KlNehoi3uViFyhtUXKSfujs6WBcl3hda+MaVUTGU78Edfa3LKcwQVjPFTwhlZwZEsmN7vr4YNUUrGFWuiTfSHQY1TtqVlGpUKat+ga0hldsNZ5BthwELCr4Ct3W/oJdncgfj8McrWeFyUfSC3Gv94K75QwAYOoVIJoH29rBcPk80LPk9l3rYgUHNUyQyXDzFhOYou0WXgwnlaup8GTYlcWWS+FNznYfY62yHE78IwsHa1TZBxX25fVAmNWlYzyt9daF11UP0/F4zlxYjnWaqAWpXvNNfo0+6CMDyHaUlWVQw==; bm_sz=FBDE0F6415C43C57911B49C8AFFC365E~YAAQbFnKF/vPczWOAQAA87mOUBeEdd2cZeRIfUNSx5dEDofAuWI9v7iHhJrelPauRalmpt1yKQj8DdAJY7CRqT2q1H+r6Ei3lVgj2CiuO72P2v2ISXp+Ol1HiwTYtWlAUtahDULwdD62aVQ8SosceYpeigks7GscYDcWcR9htqe3ErsbfCm72xbeKsI276ScAa0tX4hmWrgJ8dmKWDhI5tiq4oaS5Kgy9I1Bqi4GSezYo9aTG7Bj746uLKpn4FHUH9P2zTUn2Xht/B+YeCp5lzK3MC9ZoKaPyUrRxlrO+5cgBiScpcwchnx/oT55H/KhZQVLHwcnx6u4TbtfR6D3ZzUSnWOGFkbxf5tjk3fn~3621444~4601666")
                    .build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                String string = response.body().string();
                JsonObject responseData = gson.fromJson(string, JsonObject.class).getAsJsonObject("result");

                responseData.getAsJsonArray("stores")
                        .asList().stream().map(JsonElement::getAsJsonObject)
                        .forEach(store -> {
                            String name = store.get("name").getAsString();
                            String address = store.get("address").getAsString();
                            double lat = store.get("latitude").getAsDouble();
                            double lon = store.get("longitude").getAsDouble();
                            BaseEntity baseEntity = BaseEntity.builder()
                                    .name(name).address(address).lon(lon).lat(lat).build();
                            System.out.println(baseEntity);
                            mongoTemplate.save(baseEntity, "uniqlo_places");
                        });

                long offset = responseData.getAsJsonObject("pagination").get("offset").getAsLong();
                if (offset == 0) return;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
