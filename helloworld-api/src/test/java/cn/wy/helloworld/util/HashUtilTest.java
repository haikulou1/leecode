package cn.wy.helloworld.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * {@link HashUtil} 单元测试。
 *
 * <p>覆盖：默认 SHA-256、各算法正确性、非法算法、null 入参、大小写不敏感。</p>
 *
 * @author dtcoder
 */
public class HashUtilTest {

    @Test
    public void defaultAlgorithmIsSha256() {
        String expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
        assertThat(HashUtil.hash("abc"), is(expected));
        assertThat(HashUtil.hash("abc", HashUtil.DEFAULT_ALGORITHM), is(expected));
    }

    @Test
    public void md5Correctness() {
        // md5("abc") = 900150983cd24fb0d6963f7d28e17f72
        assertThat(HashUtil.hash("abc", "MD5"), is("900150983cd24fb0d6963f7d28e17f72"));
    }

    @Test
    public void sha1Correctness() {
        // sha-1("abc") = a9993e364706816aba3e25717850c26c9cd0d89d
        assertThat(HashUtil.hash("abc", "SHA-1"), is("a9993e364706816aba3e25717850c26c9cd0d89d"));
    }

    @Test
    public void sha512Correctness() {
        // sha-512("abc") 标准值（128 hex = 64 字节）
        String hex = HashUtil.hash("abc", "SHA-512");
        assertThat(hex.length(), is(128));
        assertThat(hex, is("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a"
                + "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f"));
    }

    @Test
    public void nonAsciiInputIsDeterministic() {
        // UTF-8 编码下的中文哈希应稳定可复现
        String once = HashUtil.hash("中文", "SHA-256");
        String twice = HashUtil.hash("中文", "SHA-256");
        assertThat(once, is(twice));
    }

    @Test
    public void algorithmIsCaseInsensitive() {
        assertThat(HashUtil.hash("abc", "sha-256"), is(HashUtil.hash("abc", "SHA-256")));
        assertThat(HashUtil.hash("abc", "  SHA-256  "), is(HashUtil.hash("abc", "SHA-256")));
    }

    @Test
    public void isSupportedAcceptsKnownAlgorithms() {
        assertTrue(HashUtil.isSupported("MD5"));
        assertTrue(HashUtil.isSupported("sha-1"));
        assertTrue(HashUtil.isSupported(" SHA-256 "));
        assertTrue(HashUtil.isSupported("SHA-512"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedAlgorithmThrows() {
        HashUtil.hash("abc", "NOT-A-HASH");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAlgorithmThrowsIllegalArgument() {
        HashUtil.hash("abc", null);
    }

    @Test(expected = NullPointerException.class)
    public void nullInputThrowsNpe() {
        HashUtil.hash(null);
    }

    @Test
    public void emptyInputProducesValidHash() {
        // sha-256("") = e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
        assertThat(HashUtil.hash("", "SHA-256"),
                is("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }

    @Test
    public void unsupportedMessageContainsAlgorithmName() {
        try {
            HashUtil.hash("abc", "ROT13");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ROT13"));
        }
    }
}
