package cn.wy.algodemo.service;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.BubbleSortResponse.Step;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 冒泡排序演示服务。
 *
 * <p>对输入整数列表执行升序冒泡排序，并记录每轮交换轨迹（round / swaps / array），
 * 不修改入参原始集合语义（内部复制排序）。</p>
 */
@Service
public class BubbleSortService {

    /**
     * 冒泡排序并记录轨迹。
     *
     * @param request 排序请求，{@code input} 为空时返回空结果
     * @return 排序响应（含 input / sorted / steps / swapCount）
     */
    public BubbleSortResponse sort(BubbleSortRequest request) {
        List<Integer> working = new ArrayList<>(request.getInput() == null
                ? Collections.<Integer>emptyList()
                : request.getInput());
        List<Integer> input = new ArrayList<>(working);
        int n = working.size();
        int totalSwaps = 0;
        List<Step> steps = new ArrayList<>();

        for (int round = 1; round < n; round++) {
            int swaps = 0;
            for (int j = 0; j < n - round; j++) {
                if (working.get(j) > working.get(j + 1)) {
                    int tmp = working.get(j);
                    working.set(j, working.get(j + 1));
                    working.set(j + 1, tmp);
                    swaps++;
                }
            }
            totalSwaps += swaps;

            Step step = new Step();
            step.setRound(round);
            step.setSwaps(swaps);
            step.setArray(new ArrayList<>(working));
            steps.add(step);
        }

        BubbleSortResponse response = new BubbleSortResponse();
        response.setInput(new ArrayList<>(input));
        response.setSorted(working);
        response.setSteps(steps);
        response.setSwapCount(totalSwaps);
        return response;
    }
}
