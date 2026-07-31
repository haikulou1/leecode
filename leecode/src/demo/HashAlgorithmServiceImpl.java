package demo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希算法接口实现：支持 MD5 与 SHA-256
 */
public class HashAlgorithmServiceImpl implements HashAlgorithmService {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    @Override
    public String hash(String input, String algorithm) {
        if (input == null) {
            input = "";
        }
        if (algorithm == null || algorithm.isEmpty()) {
            algorithm = "SHA-256";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("不支持的哈希算法: " + algorithm, e);
        }
    }

    private String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(out);
    }
}
