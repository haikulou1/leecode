/**
 * 纯 Java 单元测试（零外部依赖）。
 *
 * <p>覆盖 SortUtil 三种排序算法结果一致性、边界（空数组/单元素/null）、算法校验。
 * 运行：{@code java -ea -cp out SortUtilTest}
 */
public class SortUtilTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBubbleSort();
        testQuickSort();
        testSelectionSort();
        testAllAlgorithmsConsistent();
        testEmptyArray();
        testSingleElement();
        testNullInput();
        testAlreadySorted();
        testReverseSorted();
        testDuplicates();
        testIsSupported();
        testUnsupportedThrows();

        System.out.println("SortUtilTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testBubbleSort() {
        try {
            int[] result = SortUtil.sort("bubble", new int[]{3, 1, 2});
            assertArrayEquals(new int[]{1, 2, 3}, result, "bubble(3,1,2)");
        } catch (AssertionError e) {
            fail("testBubbleSort", e);
        }
    }

    static void testQuickSort() {
        try {
            int[] result = SortUtil.sort("quicksort", new int[]{5, 3, 1, 4, 2});
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "quicksort(5,3,1,4,2)");
        } catch (AssertionError e) {
            fail("testQuickSort", e);
        }
    }

    static void testSelectionSort() {
        try {
            int[] result = SortUtil.sort("selection", new int[]{9, 3, 7, 1, 8});
            assertArrayEquals(new int[]{1, 3, 7, 8, 9}, result, "selection(9,3,7,1,8)");
        } catch (AssertionError e) {
            fail("testSelectionSort", e);
        }
    }

    /** 三种算法对同一输入应产出相同结果。 */
    static void testAllAlgorithmsConsistent() {
        try {
            int[] input = {5, 2, 8, 1, 9, 3, 7, 4, 6, 0};
            int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            assertArrayEquals(expected, SortUtil.sort("bubble", input.clone()), "bubble consistent");
            assertArrayEquals(expected, SortUtil.sort("quicksort", input.clone()), "quicksort consistent");
            assertArrayEquals(expected, SortUtil.sort("selection", input.clone()), "selection consistent");
        } catch (AssertionError e) {
            fail("testAllAlgorithmsConsistent", e);
        }
    }

    static void testEmptyArray() {
        try {
            int[] result = SortUtil.sort("bubble", new int[0]);
            assertArrayEquals(new int[0], result, "empty array");
        } catch (AssertionError e) {
            fail("testEmptyArray", e);
        }
    }

    static void testSingleElement() {
        try {
            int[] result = SortUtil.sort("quicksort", new int[]{42});
            assertArrayEquals(new int[]{42}, result, "single element");
        } catch (AssertionError e) {
            fail("testSingleElement", e);
        }
    }

    static void testNullInput() {
        try {
            int[] result = SortUtil.sort("bubble", null);
            assertArrayEquals(new int[0], result, "null -> empty");
        } catch (AssertionError e) {
            fail("testNullInput", e);
        }
    }

    static void testAlreadySorted() {
        try {
            int[] result = SortUtil.sort("selection", new int[]{1, 2, 3});
            assertArrayEquals(new int[]{1, 2, 3}, result, "already sorted");
        } catch (AssertionError e) {
            fail("testAlreadySorted", e);
        }
    }

    static void testReverseSorted() {
        try {
            int[] result = SortUtil.sort("quicksort", new int[]{5, 4, 3, 2, 1});
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "reverse sorted");
        } catch (AssertionError e) {
            fail("testReverseSorted", e);
        }
    }

    static void testDuplicates() {
        try {
            int[] result = SortUtil.sort("bubble", new int[]{3, 1, 2, 1, 3, 2});
            assertArrayEquals(new int[]{1, 1, 2, 2, 3, 3}, result, "duplicates");
        } catch (AssertionError e) {
            fail("testDuplicates", e);
        }
    }

    static void testIsSupported() {
        try {
            assertTrue(SortUtil.isSupported("bubble"), "isSupported(bubble)");
            assertTrue(SortUtil.isSupported("quicksort"), "isSupported(quicksort)");
            assertTrue(SortUtil.isSupported("selection"), "isSupported(selection)");
            assertFalse(SortUtil.isSupported("merge"), "not isSupported(merge)");
            assertFalse(SortUtil.isSupported(""), "not isSupported(empty)");
            assertFalse(SortUtil.isSupported(null), "not isSupported(null)");
        } catch (AssertionError e) {
            fail("testIsSupported", e);
        }
    }

    static void testUnsupportedThrows() {
        try {
            SortUtil.sort("merge", new int[]{1, 2});
            fail("testUnsupportedThrows", new AssertionError("expected IAE"));
        } catch (IllegalArgumentException e) {
            pass("testUnsupportedThrows");
        } catch (AssertionError e) {
            fail("testUnsupportedThrows", e);
        }
    }

    // ---- assert helpers ----

    static void assertArrayEquals(int[] expected, int[] actual, String label) {
        if (expected.length != actual.length) {
            throw new AssertionError(label + ": length expected=" + expected.length
                    + " actual=" + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError(label + ": idx=" + i
                        + " expected=" + expected[i] + " actual=" + actual[i]);
            }
        }
        pass(label);
    }

    static void assertTrue(boolean cond, String label) {
        if (cond) {
            pass(label);
        } else {
            throw new AssertionError(label + ": expected true");
        }
    }

    static void assertFalse(boolean cond, String label) {
        if (!cond) {
            pass(label);
        } else {
            throw new AssertionError(label + ": expected false");
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
