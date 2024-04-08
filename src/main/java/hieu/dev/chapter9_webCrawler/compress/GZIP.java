package hieu.dev.chapter9_webCrawler.compress;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GZIP {
    public static byte[] gzipCompress(String str) {
        long start0 = System.currentTimeMillis();
        try (ByteArrayOutputStream obj = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
            gzip.close();
            byte[] result = obj.toByteArray();
            if(result.length != 0) {
                log.info("Compress ratio {}/{} = {}%. Time execution {} ms",
                        result.length,
                        str.getBytes().length,
                        result.length / (1.0 * str.getBytes().length) * 100,
                        System.currentTimeMillis() - start0);
            }
            return result;
        } catch (Exception e) {
            return new byte[]{};
        }
    }
    public static String gzipCompressStr(String str) {
        return new String(gzipCompress(str));
    }

    public static String gzipDecompress(byte[] str) {
        StringBuilder outStr = new StringBuilder();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str));
             BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"))) {
            String line;
            while ((line = bf.readLine()) != null) {
                outStr.append(line);
            }
            return outStr.toString();
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return "";
        }
    }
}
