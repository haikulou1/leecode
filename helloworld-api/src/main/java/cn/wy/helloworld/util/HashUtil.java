package cn.wy.helloworld.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 哈希工具类，基于 {@link MessageDigest}。
 *
 * <p>支持算法：MD5 / SHA-1 / SHA-256 / SHA-512，统一输出小写十六进制字符串。</p>
 *
 * @author dtcoder
 */
public final class HashUtil {

    private static final Logger log = LoggerFactory.getLogger(HashUtil.class);

    private HashUtil() {
    }

    public static final String DEFAULT_ALGORITHM = "SHA-256";

    /**
     * 受支持的算法集合（不可变）。
     */
    public static final Set<String> SUPPORTED_ALGORITHMS;
    private static final byte[] HEX_CHARS;

    static {
        // 保持插入顺序：MD5, SHA-1, SHA-256, SHA-512
        Set<String> algos = new LinkedHashSet<String>(
                Arrays.asList("MD5", "SHA-1", "SHA-256", "SHA-512"));
        SUPPORTED_ALGORITHMS = Collections.unmodifiableSet(algos);
        HEX_CHARS = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * 判断算法是否受支持。
     *
     * @param algorithm 算法名，大小写不敏感，允许首尾空白
     * @return true 表示受支持
     */
    public static boolean isSupported(String algorithm) {
        if (algorithm == null) {
            return false;
        }
        return SUPPORTED_ALGORITHMS.contains(algorithm.trim().toUpperCase());
    }

    /**
     * 计算给定明文的哈希值（小写十六进制），使用默认算法 SHA-256。
     *
     * @param input 待哈希的明文，不能为 null
     * @return 小写十六进制哈希串
     */
    public static String hash(String input) {
        return hash(input, DEFAULT_ALGORITHM);
    }

    /**
     * 计算给定明文在指定算法下的哈希值（小写十六进制）。
     *
     * @param input 待哈希的明文，不能为 null
     * @param algorithm 算法名，取值 MD5 / SHA-1 / SHA-256 / SHA-512，大小写不敏感
     * @return 小写十六进制哈希串
     * @throws IllegalArgumentException 算法不受支持时抛出
     * @throws NullPointerException input 为 null 时抛出
     */
    public static String hash(String input, String algorithm) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("unsupported algorithm: null");
        }
        String normalized = algorithm.trim().toUpperCase();
        if (!SUPPORTED_ALGORITHMS.contains(normalized)) {
            throw new IllegalArgumentException("unsupported algorithm: " + algorithm);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(normalized);
            byte[] raw = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(raw);
        } catch (NoSuchAlgorithmException e) {
            // normalized 已校验，此处为防御性兜底
            log.warn("MessageDigest unavailable: {}", normalized, e);
            throw new IllegalStateException("algorithm unavailable: " + normalized, e);
        }
    }

    /**
     * 将字节数组转换为小写十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 小写十六进制串
     */
    private static String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        byte[] hex = new byte[bytes.length * 2];
        int idx = 0;
        for (byte b : bytes) {
            int v = b & 0xFF;
            hex[idx++] = HEX_CHARS[v >>> 4];
            hex[idx++] = HEX_CHARS[v & 0x0F];
        }
        return new String(hex);
    }
}
