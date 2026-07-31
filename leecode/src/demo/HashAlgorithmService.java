package demo;

/**
 * 哈希算法接口
 * 对应前端第二个 tab：展示哈希算法执行结果
 */
public interface HashAlgorithmService {

    /**
     * 对输入字符串计算哈希值
     *
     * @param input     原始字符串
     * @param algorithm 算法名称，支持 MD5 / SHA-256
     * @return 哈希十六进制字符串
     */
    String hash(String input, String algorithm);
}
