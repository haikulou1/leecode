package com.antdigital.staff.common.util;

/**
 * 敏感信息脱敏工具类。
 */
public final class MaskUtil {

    private MaskUtil() {
        throw new IllegalStateException("Utility class");
    }

    /** 手机号脱敏：138****8000；null/空原样返回。 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 身份证号脱敏：110101********1234；null/空原样返回。 */
    public static String maskIdCard(String idCardNo) {
        if (idCardNo == null || idCardNo.isEmpty()) {
            return idCardNo;
        }
        if (idCardNo.length() <= 8) {
            return "****";
        }
        return idCardNo.substring(0, 6) + "********" + idCardNo.substring(idCardNo.length() - 4);
    }
}