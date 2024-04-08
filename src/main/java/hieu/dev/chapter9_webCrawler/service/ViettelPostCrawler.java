package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.client.BaseHttpClient;
import hieu.dev.chapter9_webCrawler.compress.GZIP;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ViettelPostCrawler {
    public static void main(String[] args) throws IOException {
        Request request = new Request.Builder()
                .url("https://tiles.viettelpost.vn/data/v3/14/12989/7296.pbf")
                .get()
                .addHeader("Referer", "https://store-locator.okd.viettelpost.vn/")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .build();
        Response response = BaseHttpClient.okHttpClient.newCall(request).execute();
        byte[] responseBytes = response.body().bytes();
        String responseStr = GZIP.gzipDecompress(responseBytes);
        System.out.println(responseStr);

    }
}
