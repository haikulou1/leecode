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
     * <p><b>非安全用途</b>：MD5/SHA-1 已不再适合口令存储、完整性校验等安全场景；
     * 如需安全哈希请优先使用 {@link #sha256(String)}。
     *
     * @param input     待哈希内容，不允许为 null
     * @param algorithm 算法名，如 "MD5"、"SHA-1"、"SHA-256"
     * @return 小写十六进制哈希值
     */
    String hash(String input, String algorithm);

    /**
     * 计算 MD5 哈希的便捷方法。
     * <p><b>非安全用途</b>：MD5 已被证明存在碰撞，禁止用于口令存储、完整性校验等
     * 安全场景；如需安全哈希请优先使用 {@link #sha256(String)}。
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
