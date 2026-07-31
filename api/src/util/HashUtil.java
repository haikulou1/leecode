import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希算法工具类（基于 JDK {@link MessageDigest}，零外部依赖）。
 *
 * <p>支持算法：MD5 / SHA-1 / SHA-256 / SHA-512，返回小写十六进制摘要。
 */
public class HashUtil {

    /**
     * 计算指定算法的消息摘要（十六进制小写）。
     *
     * @param algorithm 算法名：md5 / sha1 / sha256 / sha512
     * @param input     原文
     * @return 十六进制摘要字符串
     * @throws IllegalArgumentException 算法不支持
     */
    public static String digest(String algorithm, String input) {
        String alg = normalize(algorithm);
        try {
            MessageDigest md = MessageDigest.getInstance(alg);
            byte[] raw = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(raw);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[ERROR] [HashUtil] digest failed: algorithm=" + algorithm
                    + ", inputLen=" + input.length()
                    + ", error=" + e.getMessage());
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
        }
    }

    /**
     * 校验算法是否被支持。
     */
    public static boolean isSupported(String algorithm) {
        if (algorithm == null) {
            return false;
        }
        switch (normalize(algorithm)) {
            case "MD5":
            case "SHA-1":
            case "SHA-256":
            case "SHA-512":
                return true;
            default:
                return false;
        }
    }

    /** 统一算法名为 MessageDigest 接受的标准形式。 */
    private static String normalize(String algorithm) {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: algorithm");
        }
        String a = algorithm.trim().toLowerCase();
        switch (a) {
            case "md5":
                return "MD5";
            case "sha1":
            case "sha-1":
                return "SHA-1";
            case "sha256":
            case "sha-256":
                return "SHA-256";
            case "sha512":
            case "sha-512":
                return "SHA-512";
            default:
                return a; // 交给 MessageDigest 抛 NoSuchAlgorithmException
        }
    }

    /** 字节数组转十六进制小写字符串。 */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
