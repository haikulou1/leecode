package com.antdigital.staff.common.constant;

/**
 * 人员看板应用级常量（按功能归类，不搞大而全）。
 */
public final class StaffConstants {

    private StaffConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** 分页默认值。 */
    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 200;

    /** 租户上下文缺省值（无登录态上下文时使用，生产由统一拦截器注入真实 tenant）。 */
    public static final String DEFAULT_TENANT_ID = "default";

    /** 员工数据来源。 */
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_IMPORT = "IMPORT";

    /** 员工身份证号加密算法。 */
    public static final String ID_CARD_CIPHER = "AES";
}