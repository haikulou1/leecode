package cn.wy.algodemo.model;

/**
 * 导出请求体（POST /api/export）。
 *
 * <p>携带前端各 Tab 已执行的实际结果，由后端格式化为纯文本导出。
 * 各模块字段可选，为 null 时后端用内置示例兜底。</p>
 */
public class ExportRequest {

    /** 导出范围：helloworld | hash | bubble-sort | all（默认 all）。 */
    private String tab;

    /** HelloWorld 结果（可选）。 */
    private HelloResult hello;

    /** Hash 结果（可选）。 */
    private HashResponse hash;

    /** BubbleSort 结果（可选）。 */
    private BubbleSortResponse bubble;

    public String getTab() { return tab; }
    public void setTab(String tab) { this.tab = tab; }

    public HelloResult getHello() { return hello; }
    public void setHello(HelloResult hello) { this.hello = hello; }

    public HashResponse getHash() { return hash; }
    public void setHash(HashResponse hash) { this.hash = hash; }

    public BubbleSortResponse getBubble() { return bubble; }
    public void setBubble(BubbleSortResponse bubble) { this.bubble = bubble; }

    /** HelloWorld 结果片段。 */
    public static class HelloResult {
        private String result;
        private String timestamp;

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
