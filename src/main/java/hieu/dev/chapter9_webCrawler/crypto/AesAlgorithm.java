package hieu.dev.chapter9_webCrawler.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class AesAlgorithm {
    public static String decrypt(String cypherText) throws Exception {
        return decrypt(cypherText, "b14ca5898a4e4133bbce2ea2315a1916");
    }
    public static String decrypt(String cypherText, String key) throws Exception {
        IvParameterSpec ivParameterSpec = generateEmptyIvSpec();
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cypherText));
        return new String(plainText, StandardCharsets.UTF_8);
    }

    private static IvParameterSpec generateEmptyIvSpec() {
        byte[] bytes = new byte[16];
        Arrays.fill(bytes, (byte) 0);
        return new IvParameterSpec(bytes);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(decrypt("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"));
    }
}
