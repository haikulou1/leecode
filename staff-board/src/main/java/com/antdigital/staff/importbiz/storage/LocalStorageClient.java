package com.antdigital.staff.importbiz.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储实现（默认）。生产环境可切换对象存储，见 {@link StorageClient}。
 */
@Component
public class LocalStorageClient implements StorageClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalStorageClient.class);

    private final Path rootDir;

    public LocalStorageClient(@Value("${staff.import.local-storage-dir:./data/staff-board}") String rootDir) {
        this.rootDir = Paths.get(rootDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException ex) {
            throw new UncheckedIOException("初始化文件存储目录失败", ex);
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
        String uniqueName = UUID.randomUUID() + "_" + safeName(fileName);
        Path target = rootDir.resolve(uniqueName);
        try {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("存储文件失败", ex);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("文件已存储: {}", target);
        }
        return target.toString();
    }

    @Override
    public InputStream download(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        Path path = Paths.get(fileUrl);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException ex) {
            throw new UncheckedIOException("读取文件失败", ex);
        }
    }

    private String safeName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "file";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}