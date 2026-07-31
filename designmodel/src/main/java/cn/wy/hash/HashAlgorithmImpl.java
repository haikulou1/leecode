package cn.wy.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link HashAlgorithm} 的默认实现，基于 JDK {@link MessageDigest}。
 * <p>输出小写十六进制串。
 *
 * @author wy
 */
public class HashAlgorithmImpl implements HashAlgorithm {

    private static final Logger LOGGER = Logger.getLogger(HashAlgorithmImpl.class.getName());

    /** {@inheritDoc} */
    @Override
    public String hash(String input, String algorithm) {
        if (input == null) {
            throw new NullPointerException("input must not be null");
        }
        if (algorithm == null || algorithm.length() == 0) {
            throw new IllegalArgumentException("algorithm must not be empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            return toHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.WARNING, "unsupported algorithm: {0}", algorithm);
            throw new IllegalArgumentException("unsupported algorithm: " + algorithm, e);
        } catch (java.io.UnsupportedEncodingException e) {
            // UTF-8 为 JDK 必备编码，理论上不会到达
            LOGGER.log(Level.SEVERE, "UTF-8 not supported, environment is broken", e);
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String md5(String input) {
        return hash(input, "MD5");
    }

    /** {@inheritDoc} */
    @Override
    public String sha256(String input) {
        return hash(input, "SHA-256");
    }

    /**
     * 将字节数组转为小写十六进制串。
     *
     * @param bytes 字节数组
     * @return 小写十六进制字符串
     */
    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
