package com.antdigital.staff.common.context;

import com.antdigital.staff.common.constant.StaffConstants;

/**
 * 当前请求上下文（登录态由全局统一拦截器注入，这里只提供租户读取入口）。
 */
public final class StaffContextHolder {

    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

    private StaffContextHolder() {
        throw new IllegalStateException("Utility class");
    }

    /** 将登录态上下文中的租户 ID 注入当前线程。 */
    public static void setTenantId(String tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    /** 读取当前租户 ID；未注入时返回默认值，避免空指针。 */
    public static String getTenantId() {
        String tenantId = TENANT_HOLDER.get();
        return tenantId == null || tenantId.isEmpty() ? StaffConstants.DEFAULT_TENANT_ID : tenantId;
    }

    /** 清理线程副本，防止线程复用导致租户串号。 */
    public static void clear() {
        TENANT_HOLDER.remove();
    }
}