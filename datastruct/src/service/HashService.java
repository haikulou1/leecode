package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import util.JsonUtil;

/**
 * 哈希服务：F1.2 接口。
 * 对入参做 SHA-256，返回 64 位十六进制小写字符串。
 */
public class HashService {

    private static final String ALGORITHM = "SHA-256";

    /**
     * 对 input 做 SHA-256，返回 JSON 字符串。
     */
    public String hash(String input) {
        String hex = sha256Hex(input);
        StringBuilder sb = new StringBuilder(128);
        sb.append('{');
        sb.append(JsonUtil.quoteField("input", input));
        sb.append(',');
        sb.append(JsonUtil.quoteField("algorithm", ALGORITHM));
        sb.append(',');
        sb.append(JsonUtil.quoteField("hash", hex));
        sb.append(',');
        sb.append(JsonUtil.rawField("length", String.valueOf(hex.length())));
        sb.append('}');
        return sb.toString();
    }

    /**
     * 计算给定字符串的 SHA-256 十六进制小写形式。
     * 供 ExportService 复用。
     */
    public String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHexLower(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 为 JDK 标准算法，理论不会缺失
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 将字节数组转为十六进制小写字符串。
     * SHA-256 输出 32 字节 -> 64 字符，与 length:64 对齐。
     */
    private String toHexLower(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
