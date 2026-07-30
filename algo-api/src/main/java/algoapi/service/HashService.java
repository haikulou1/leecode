package algoapi.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希算法服务（SHA-256），NoSuchAlgorithmException 兜底回退 MD5
 *
 * CR 修复（2026-07-30）：
 * - I6: 极端兜底不再返回 input.length() 伪哈希，改为抛 IllegalStateException
 */
@Service
public class HashService {

    /**
     * 计算输入字符串的 SHA-256 哈希值（hex）
     * 若 SHA-256 不可用（理论不可达，JDK 内置），兜底回退 MD5
     */
    public String[] hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return new String[]{"SHA-256", toHex(digest)};
        } catch (NoSuchAlgorithmException e) {
            // 兜底：回退 MD5
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
                return new String[]{"MD5(fallback)", toHex(digest)};
            } catch (NoSuchAlgorithmException ex) {
                // I6: 极端兜底抛异常，让全局处理器兜底 500，不返回伪哈希值
                throw new IllegalStateException("哈希算法不可用（SHA-256 和 MD5 均无法初始化）", ex);
            }
        }
    }

    /**
     * 字节数组转 hex 字符串
     */
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
