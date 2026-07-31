package cn.wy.hash;

/**
 * 哈希算法接口。
 * <p>基于 JDK {@link java.security.MessageDigest} 封装，支持 MD5 / SHA-1 / SHA-256。
 *
 * @author wy
 */
public interface HashAlgorithm {

    /**
     * 对输入串按指定算法计算哈希，返回小写十六进制串。
     *
     * @param input     待哈希内容，不允许为 null
     * @param algorithm 算法名，如 "MD5"、"SHA-1"、"SHA-256"
     * @return 小写十六进制哈希值
     */
    String hash(String input, String algorithm);

    /**
     * 计算 MD5 哈希的便捷方法。
     *
     * @param input 待哈希内容，不允许为 null
     * @return 小写十六进制 MD5 值
     */
    String md5(String input);

    /**
     * 计算 SHA-256 哈希的便捷方法。
     *
     * @param input 待哈希内容，不允许为 null
     * @return 小写十六进制 SHA-256 值
     */
    String sha256(String input);
}
