package hieu.dev.chapter7_shortUrl.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class CRC32_Manual {
    private static final long polynomial = 0b100000100110000010001110110110111L;
    private static final int baseSize = 33;

    public static void main(String[] args) {
        String inputString = "hello crc32";

        CRC32 crc32 = new CRC32();
        crc32.update(inputString.getBytes());
        System.out.println(Long.toHexString(crc32.getValue()));

//        long crc32Hash = CRC32_Manual.crc32(0b11010011101100L);
//        System.out.println(Long.toBinaryString(crc32Hash));

        long crc32Hashing = CRC32_Manual.crc32(inputString);
        System.out.println(Long.toHexString(crc32Hashing));
    }

    public static long crc32(Object input) {
        long crc32Hash = CRC32_Manual.getValue(0b11010011101100L);
        crc32Hash = crc32Hash << baseSize - 1;
        return CRC32_Manual.getValue(crc32Hash);
    }

    public static long getValue(Object input) {
        List<Byte> bits = new ArrayList<>();
        if (input instanceof String) {
            bits = convertToList((String) input);
        } else if (input instanceof Long) {
            bits = convertToList((Long) input);
        }

        List<Byte> remainder = new ArrayList<>();

        for (int i = 0; i < bits.size(); ) {
            int countSize = 0;
            while (remainder.size() + countSize < baseSize && countSize + i < bits.size()) {
                countSize++;
            }
            remainder.addAll(bits.subList(i, i + countSize));
            while (remainder.get(0) == 0) {
                remainder = remainder.subList(1, remainder.size());
            }

            if (remainder.size() == baseSize) {
                long remainderNumber = convertToNumber(remainder) ^ polynomial;
                remainder = convertToList(remainderNumber);
            }
            i += countSize;
        }
        return convertToNumber(remainder);
    }

    private static List<Byte> convertToList(long number) {
        if(number == 0) return new ArrayList<>();
        List<Byte> bits = new ArrayList<>();
        for (long i = 1; i <= number; i <<= 1) {
            if ((number & i) != 0) {
                bits.add(0, (byte) 1);
            } else {
                bits.add(0, (byte) 0);
            }
        }
        while (!bits.isEmpty() && bits.get(0) == 0) {
            bits = bits.subList(1, bits.size());
        }
        return bits;
    }

    private static List<Byte> convertToList(String string) {
        List<Byte> bits = new ArrayList<>();
        for (int i = 0; i < string.length(); i++) {
            List<Byte> listBytes = convertToList(string.charAt(i));
            while (listBytes.size() < 8) {
                listBytes.add(0, (byte)0);
            }
            bits.addAll(listBytes);
        }
        return bits;
    }

    private static long convertToNumber(List<Byte> bytes) {
        long number = 0;
        for (int i = 0; i < bytes.size(); i++) {
            number += (long) (Math.pow(2, bytes.size() - 1 - i) * bytes.get(i));
        }
        return number;
    }

    private static final long divisor = 0xEDB88320L;
    private static final long unit1 = 0xFFFFFFFFL;
    private static long toUnsignedInt32(long n) {
        if (n >= 0) {
            return n;
        }
        return unit1 - (n * -1) + 1;
    }

    private static long crc32UnsignedFull(String input) {
        byte[] bytes = input.getBytes();
        var crc = unit1;
        for (var c : bytes) {
            crc = (crc ^ c);
            for (var i = 0; i < 8; i++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ divisor;
                } else {
                    crc = crc >>> 1;
                }
            }
        }
        return toUnsignedInt32(~crc);
    }
}
