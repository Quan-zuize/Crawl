package hieu.dev.chapter7_shortUrl.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;

@Service
@Slf4j
public class TwitterIdGenerator {
    /** Based on Twitter snowflake algorithm
     *  - timestamp = 2 ^ 41 / 1000 / 3600 / 24 / 365 = 69 years
     *  - nodeId = 2 ^ 10 = 1024 differentiate by mac address
     *  - sequence = 2 ^ 12 = 4096 req/mills = 4m req/s
     * **/
    private static final long startTimeSeconds = 1705385447000L;
    private static final int NODE_BIT = 10;
    private static final int SEQUENCE_BIT = 12;
    private long lastTime;
    private int sequence;
    private long nodeId;

    @PostConstruct
    public void init() {
        lastTime = -1L;
        sequence = 0;
        nodeId = getNodeIdByMacAddress();
    }

    public long generateId() {
        long currentTime = getTimestamp();
        long sequence = getSequenceNumber(currentTime);

        long id = currentTime << NODE_BIT;
        id = id | nodeId;
        id = id << SEQUENCE_BIT;
        id = id | sequence;
        return id;
    }

    public static long getTimestamp() {
        return System.currentTimeMillis() - startTimeSeconds;
    }
    public static long getNodeIdByMacAddress() {
        int maxNodeBit = (int) Math.pow(2, NODE_BIT) - 1;

        int newNodeId;
        try {
            InetAddress ip = Inet4Address.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ip);
            byte[] mac = networkInterface.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            new SecureRandom().nextInt();
            for (byte b : mac) {
                sb.append(String.format("%02X", b));
            }

            log.info("Current MAC: {} - {}\n", sb, networkInterface.getName());
            newNodeId = sb.hashCode();
        } catch (Exception ex) {
            newNodeId = (new SecureRandom().nextInt());
        }
        return newNodeId & maxNodeBit;
    }

    public synchronized long getSequenceNumber(long currentTime) {
        int maxSequenceBit = (int) Math.pow(2, SEQUENCE_BIT) - 1;
        if(currentTime == lastTime) {
            sequence ++;
        } else {
            sequence = 0;
            lastTime = currentTime;
        }
        return sequence & maxSequenceBit;
    }
}
