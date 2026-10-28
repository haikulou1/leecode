package com.antdigital.staff.importbiz.storage;

import java.io.InputStream;

/**
 * 文件存储抽象：导入文件与错误报告存储，默认本地实现，可切换 OSS/MinIO。
 */
public interface StorageClient {

    /** 上传文件，返回文件地址；fileName 仅作命名参考，实现内部保证唯一。 */
    String upload(InputStream inputStream, String fileName);

    /** 按地址读取文件流；文件不存在时返回 null。 */
    InputStream download(String fileUrl);
}