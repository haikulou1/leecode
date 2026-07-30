package com.leecode.webdemo.common;

/**
 * 算法服务相关错误码与业务常量。
 *
 * @author DTCoder
 */
public final class AlgorithmConstants {

    private AlgorithmConstants() {
    }

    // ===== 哈希算法错误码 =====
    /** 不支持的哈希算法 */
    public static final String ALGORITHM_001 = "ALGORITHM_001";
    /** 哈希输入为空 */
    public static final String ALGORITHM_002 = "ALGORITHM_002";

    // ===== 排序错误码 =====
    /** 排序数组为空 */
    public static final String SORT_001 = "SORT_001";
    /** 不支持的排序方向 */
    public static final String SORT_002 = "SORT_002";

    // ===== 导出错误码 =====
    /** 不支持的导出类型 */
    public static final String EXPORT_001 = "EXPORT_001";
    /** 不支持的导出格式 */
    public static final String EXPORT_002 = "EXPORT_002";
    /** 哈希导出缺少输入 */
    public static final String EXPORT_003 = "EXPORT_003";
    /** 排序导出缺少数组 */
    public static final String EXPORT_004 = "EXPORT_004";

    // ===== 业务常量 =====
    /** 冒泡排序数组最大长度（超过则截断兜底） */
    public static final int MAX_SORT_ARRAY_SIZE = 1000;
    /** 默认问候语 */
    public static final String HELLO_WORLD_MESSAGE = "Hello World";
    /** 兜底导出标记 */
    public static final String FALLBACK_EXPORT_MSG = "兜底导出";
}
