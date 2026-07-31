package cn.wy.algodemo.service;

import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希算法演示服务。
 *
 * <p>支持 MD5 / SHA-256 / SHA-512，默认 SHA-256；hash 输出为十六进制小写。</p>
 */
@Service
public class HashService {

    /** algorithm 为空时使用的默认算法。 */
    private static final String DEFAULT_ALGORITHM = "SHA-256";

    /**
     * 对输入文本计算哈希。
     *
     * @param request 哈希请求，{@code input} 为空按空串处理，{@code algorithm} 为空默认 SHA-256
     * @return 哈希响应（含 input / algorithm / hash / length）
     */
    public HashResponse hash(HashRequest request) {
        String input = request.getInput() == null ? "" : request.getInput();
        String algorithm = request.getAlgorithm();
        if (algorithm == null || algorithm.isEmpty()) {
            algorithm = DEFAULT_ALGORITHM;
        }
        String hex = digest(input, algorithm);

        HashResponse response = new HashResponse();
        response.setInput(input);
        response.setAlgorithm(algorithm);
        response.setHash(hex);
        response.setLength(hex.length());
        return response;
    }

    /**
     * 计算指定算法的十六进制小写哈希。
     *
     * @param input     原文
     * @param algorithm JCE 算法名（MD5 / SHA-256 / SHA-512）
     * @return 十六进制小写哈希字符串
     */
    private String digest(String input, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported hash algorithm: " + algorithm, e);
        }
    }

    /**
     * 字节数组转十六进制小写字符串。
     */
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }
}
