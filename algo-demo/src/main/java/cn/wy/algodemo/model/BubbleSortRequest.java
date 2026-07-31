package cn.wy.algodemo.model;

import java.util.List;

/**
 * 冒泡排序请求体。
 *
 * <p>字段：{@code input}（待排序的整数列表）。</p>
 */
public class BubbleSortRequest {

    /** 待排序的整数列表。 */
    private List<Integer> input;

    public List<Integer> getInput() {
        return input;
    }

    public void setInput(List<Integer> input) {
        this.input = input;
    }
}
