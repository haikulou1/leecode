package cn.wy.hash;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link HashAlgorithmImpl} 单元测试，使用标准哈希向量校验。
 *
 * @author wy
 */
public class HashAlgorithmTest {

    private final HashAlgorithm hash = new HashAlgorithmImpl();

    @Test
    public void md5_abc_matchesStandardVector() {
        // "abc" 的 MD5 标准值
        Assert.assertEquals("900150983cd24fb0d6963f7d28e17f72", hash.md5("abc"));
    }

    @Test
    public void sha256_abc_matchesStandardVector() {
        // "abc" 的 SHA-256 标准值
        Assert.assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                hash.sha256("abc"));
    }

    @Test
    public void hash_sha1_matchesStandardVector() {
        // "abc" 的 SHA-1 标准值
        Assert.assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", hash.hash("abc", "SHA-1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hash_unknownAlgorithm_throws() {
        hash.hash("abc", "NOT-A-REAL-ALGO");
    }

    @Test(expected = NullPointerException.class)
    public void md5_nullInput_throws() {
        hash.md5(null);
    }
}
