package hieu.dev.chapter9_webCrawler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
public class CertUtils {
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }};

    public static void ignoreCertificates() {
        log.info("Ignore all certs");
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            log.error("Error while ignore all certs {}", e.getMessage(), e);
        }
    }

    public static OkHttpClient okHttpClientWithoutTrust() {
        try {
            return new OkHttpClient.Builder()
                    .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            log.error("Error while ignore all certs {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
