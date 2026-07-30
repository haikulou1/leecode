package com.leecode.webdemo.service;

import com.leecode.webdemo.dto.ExportRequest;

/**
 * 导出服务接口，复用算法服务格式化为 CSV/JSON 文件流。
 *
 * @author DTCoder
 */
public interface ExportService {

    /**
     * 执行导出，返回文件内容字节数组。
     *
     * @param request 导出请求
     * @return 文件内容（CSV 或 JSON 字节）
     */
    byte[] export(ExportRequest request);

    /**
     * 获取导出文件扩展名。
     *
     * @param request 导出请求
     * @return 文件扩展名（csv / json）
     */
    String getFileExtension(ExportRequest request);
}
