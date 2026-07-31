package cn.wy.algodemo.model;

/**
 * 哈希算法请求体。
 *
 * <p>字段：{@code input}（待哈希的原文）、{@code algorithm}（MD5/SHA-256/SHA-512，默认 SHA-256）。</p>
 */
public class HashRequest {

    /** 待哈希的原文，空串按空串处理。 */
    private String input;

    /** 哈希算法名称，可选 MD5 / SHA-256 / SHA-512，为空时默认 SHA-256。 */
    private String algorithm;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
