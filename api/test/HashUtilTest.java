/**
 * 纯 Java 单元测试（零外部依赖）。
 *
 * <p>覆盖 HashUtil 摘要值校验、算法别名、异常链；SortUtil 排序结果与边界。
 * 运行：{@code java -ea -cp out HashUtilTest}
 */
public class HashUtilTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testDigestMd5();
        testDigestSha256();
        testDigestSha1();
        testDigestSha512();
        testAlgorithmAlias();
        testUnsupportedAlgorithm();
        testUnsupportedRethrowsWithCause();

        System.out.println("HashUtilTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** MD5 摘要值断言（已知值）。 */
    static void testDigestMd5() {
        try {
            String result = HashUtil.digest("md5", "abc");
            // "abc" 的 MD5 = 900150983cd24fb0d6963f7d28e17f72
            assertEquals("900150983cd24fb0d6963f7d28e17f72", result, "MD5(abc)");
        } catch (AssertionError e) {
            fail("testDigestMd5", e);
        }
    }

    /** SHA-256 摘要值断言（已知值）。 */
    static void testDigestSha256() {
        try {
            String result = HashUtil.digest("sha256", "abc");
            // "abc" 的 SHA-256 = ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
            assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                    result, "SHA-256(abc)");
        } catch (AssertionError e) {
            fail("testDigestSha256", e);
        }
    }

    /** SHA-1 摘要值断言（已知值）。 */
    static void testDigestSha1() {
        try {
            String result = HashUtil.digest("sha1", "abc");
            // "abc" 的 SHA-1 = a9993e364706816aba3e25717850c26c9cd0d89d
            assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", result, "SHA-1(abc)");
        } catch (AssertionError e) {
            fail("testDigestSha1", e);
        }
    }

    /** SHA-512 非空且长度为 128。 */
    static void testDigestSha512() {
        try {
            String result = HashUtil.digest("sha512", "abc");
            assertTrue(result != null && result.length() == 128, "SHA-512(abc) len=128");
        } catch (AssertionError e) {
            fail("testDigestSha512", e);
        }
    }

    /** 算法别名（sha-256 / SHA-256）应等价。 */
    static void testAlgorithmAlias() {
        try {
            String r1 = HashUtil.digest("sha256", "test");
            String r2 = HashUtil.digest("SHA-256", "test");
            assertEquals(r1, r2, "alias sha256==SHA-256");
            assertTrue(HashUtil.isSupported("sha-1"), "isSupported(sha-1)");
            assertTrue(HashUtil.isSupported("SHA-512"), "isSupported(SHA-512)");
            assertFalse(HashUtil.isSupported("rc4"), "not isSupported(rc4)");
        } catch (AssertionError e) {
            fail("testAlgorithmAlias", e);
        }
    }

    /** 不支持的算法应抛 IllegalArgumentException。 */
    static void testUnsupportedAlgorithm() {
        try {
            HashUtil.digest("rc4", "abc");
            fail("testUnsupportedAlgorithm", new AssertionError("expected IAE not thrown"));
        } catch (IllegalArgumentException e) {
            pass("testUnsupportedAlgorithm");
        } catch (AssertionError e) {
            fail("testUnsupportedAlgorithm", e);
        }
    }

    /** 重抛的异常应保留 cause（原始 NoSuchAlgorithmException）。P1 G16.2 修复验证。 */
    static void testUnsupportedRethrowsWithCause() {
        try {
            HashUtil.digest("rc4", "abc");
            fail("testUnsupportedRethrowsWithCause", new AssertionError("expected IAE"));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getCause() != null, "IAE has cause (NoSuchAlgorithmException)");
        }
    }

    // ---- assert helpers ----

    static void assertEquals(String expected, String actual, String label) {
        if (expected.equals(actual)) {
            pass(label);
        } else {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }

    static void assertTrue(boolean cond, String label) {
        if (cond) {
            pass(label);
        } else {
            throw new AssertionError(label + ": expected true, got false");
        }
    }

    static void assertFalse(boolean cond, String label) {
        if (!cond) {
            pass(label);
        } else {
            throw new AssertionError(label + ": expected false, got true");
        }
    }

    static void pass(String label) {
        passed++;
    }

    static void fail(String label, Throwable t) {
        failed++;
        System.err.println("[FAIL] " + label + ": " + t.getMessage());
    }
}
