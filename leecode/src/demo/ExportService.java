package demo;

/**
 * 导出接口
 * 后台提供导出能力，支持导出各个页面的展示结果
 * 导出格式统一为 JSON：{ "name": "...", "result": "...", "timestamp": "..." }
 */
public interface ExportService {

    /**
     * 将单个页面结果导出为文件
     *
     * @param name      结果名称
     * @param result    结果内容
     * @param timestamp 时间戳
     * @return 导出文件的物理路径
     */
    String export(String name, String result, String timestamp);

    /**
     * 将单个页面结果导出为文件，自动使用当前时间
     *
     * @param name   结果名称
     * @param result 结果内容
     * @return 导出文件的物理路径
     */
    String export(String name, String result);
}
