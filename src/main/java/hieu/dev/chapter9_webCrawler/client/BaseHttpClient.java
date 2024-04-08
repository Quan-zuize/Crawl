package hieu.dev.chapter9_webCrawler.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hieu.dev.chapter9_webCrawler.CertUtils;
import hieu.dev.chapter9_webCrawler.Utils;
import hieu.dev.chapter9_webCrawler.crypto.AesAlgorithm;
import hieu.dev.chapter9_webCrawler.dto.GovSearchResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509TrustManager;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseHttpClient {
    public static final RestTemplate restTemplate = new RestTemplate();
    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    public static final HttpHeaders headers = new HttpHeaders();

    public static final OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(null)
            .connectTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .build();

    static {
        CertUtils.ignoreCertificates();
        headers.add("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36");
        headers.setExpires(0);
        headers.setCacheControl("private, no-store, max-age=0");
//        headers.add("accept", "*/*");
//        headers.add("accept-encoding", "gzip, deflate, br");
    }

    public static <T> T callApiAndDecrypt(String url, HttpHeaders headers, Class<T> tClass) throws Exception {
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> forEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String encryptedData = forEntity.getBody();
        String data = AesAlgorithm.decrypt(encryptedData);
        return gson.fromJson(data, tClass);
    }
}
