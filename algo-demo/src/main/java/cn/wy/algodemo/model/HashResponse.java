package cn.wy.algodemo.model;

/**
 * 哈希算法响应体。
 *
 * <p>字段：{@code input}、{@code algorithm}、{@code hash}（十六进制小写）、{@code length}（hash 字符串长度）。</p>
 */
public class HashResponse {

    /** 原文输入。 */
    private String input;

    /** 实际使用的算法名称。 */
    private String algorithm;

    /** 十六进制小写哈希值。 */
    private String hash;

    /** hash 字符串长度。 */
    private int length;

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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
