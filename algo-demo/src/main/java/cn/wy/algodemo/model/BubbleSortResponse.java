package cn.wy.algodemo.model;

import java.util.List;

/**
 * 冒泡排序响应体。
 *
 * <p>字段：{@code input}（原始输入）、{@code sorted}（升序结果）、
 * {@code steps}（每轮交换轨迹）、{@code swapCount}（总交换次数）。</p>
 */
public class BubbleSortResponse {

    /** 原始输入数组。 */
    private List<Integer> input;

    /** 升序排序结果。 */
    private List<Integer> sorted;

    /** 每轮交换轨迹。 */
    private List<Step> steps;

    /** 总交换次数。 */
    private int swapCount;

    /**
     * 单轮交换轨迹。
     *
     * <p>{@code round} 从 1 开始；{@code swaps} 为该轮交换次数；
     * {@code array} 为该轮结束时的数组状态快照。</p>
     */
    public static class Step {

        /** 轮次，从 1 开始。 */
        private int round;

        /** 该轮交换次数。 */
        private int swaps;

        /** 该轮结束时的数组状态快照。 */
        private List<Integer> array;

        public int getRound() {
            return round;
        }

        public void setRound(int round) {
            this.round = round;
        }

        public int getSwaps() {
            return swaps;
        }

        public void setSwaps(int swaps) {
            this.swaps = swaps;
        }

        public List<Integer> getArray() {
            return array;
        }

        public void setArray(List<Integer> array) {
            this.array = array;
        }
    }

    public List<Integer> getInput() {
        return input;
    }

    public void setInput(List<Integer> input) {
        this.input = input;
    }

    public List<Integer> getSorted() {
        return sorted;
    }

    public void setSorted(List<Integer> sorted) {
        this.sorted = sorted;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public int getSwapCount() {
        return swapCount;
    }

    public void setSwapCount(int swapCount) {
        this.swapCount = swapCount;
    }
}
