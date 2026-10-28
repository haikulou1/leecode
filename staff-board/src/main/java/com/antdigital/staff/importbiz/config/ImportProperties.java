package com.antdigital.staff.importbiz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 导入相关配置项：maxRows、白名单强校验开关、导入开关、本地存储目录。
 */
@Component
@ConfigurationProperties(prefix = "staff.import")
public class ImportProperties {

    /** 单批最大行数。 */
    private int maxRows = 5000;

    /** 白名单强校验开关（可灰度/可应急）。 */
    private boolean whitelistEnforced = true;

    /** 导入功能开关（可应急关闭）。 */
    private boolean enableImport = true;

    /** 本地文件存储根目录。 */
    private String localStorageDir = "./data/staff-board";

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public boolean isWhitelistEnforced() {
        return whitelistEnforced;
    }

    public void setWhitelistEnforced(boolean whitelistEnforced) {
        this.whitelistEnforced = whitelistEnforced;
    }

    public boolean isEnableImport() {
        return enableImport;
    }

    public void setEnableImport(boolean enableImport) {
        this.enableImport = enableImport;
    }

    public String getLocalStorageDir() {
        return localStorageDir;
    }

    public void setLocalStorageDir(String localStorageDir) {
        this.localStorageDir = localStorageDir;
    }
}