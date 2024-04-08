package hieu.dev.chapter7_shortUrl.algorithm;

import hieu.dev.chapter7_shortUrl.Utils7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Base62 {
    private static final List<Character> ALPHABET = Arrays.asList(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    );

    public static String encode(long id) {
        List<String> resultList = new ArrayList<>();
        while (id != 0) {
            char c = ALPHABET.get((int) (id % ALPHABET.size()));
            resultList.add(String.valueOf(c));
            id = id / ALPHABET.size();
        }
        Collections.reverse(resultList);
        return String.join("", resultList);
    }

    public static long decode(String data) {
        long result = 0;
        for (int i = data.length() - 1; i >= 0; i--) {
            int base62Value = ALPHABET.indexOf(data.charAt(i));
            result += (long) (base62Value * Math.pow(ALPHABET.size(), data.length() - 1 - i));
        }
        return result;
    }

    public static void main(String[] args) {
        String inputString = Utils7.generateLongStringUrl(40);

    }
}
