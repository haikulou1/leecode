package com.antdigital.staff.common.config;

import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.common.enums.StaffErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * 租户上下文过滤器：校验并承接租户 ID 到线程上下文，请求结束清理。
 *
 * <p>安全模型（P0-1 修复）：{@code X-Tenant-Id} 由客户端可任意伪造，禁止裸信任。
 * 本过滤器要求请求携带对租户 ID 计算的 HMAC-SHA256 签名 {@code X-Tenant-Signature}
 * （签名密钥 {@code STAFF_TENANT_SIGNING_KEY}），签名缺失或校验失败一律拒绝；
 * 未配置签名密钥则应用启动失败（fail-fast），避免以不安全状态上线。
 * 调用方身份（登录态/网关鉴权）由统一网关完成，本过滤器补足「租户头不可伪造」的兜底。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String SIGNATURE_HEADER = "X-Tenant-Signature";
    private static final Pattern TENANT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec signingKey;

    public TenantContextFilter(@Value("${staff.security.tenant-signing-key:}") String base64SigningKey) {
        if (base64SigningKey == null || base64SigningKey.trim().isEmpty()) {
            throw new IllegalStateException("staff.security.tenant-signing-key 未配置，禁止裸信任 X-Tenant-Id（P0-1 发布阻断）");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64SigningKey);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("staff.security.tenant-signing-key 非合法 Base64", ex);
        }
        if (keyBytes.length < 16) {
            throw new IllegalStateException("staff.security.tenant-signing-key 长度非法（需至少 16 字节）");
        }
        this.signingKey = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId == null) {
            // 未携带租户头：不注入租户上下文（读取时回退默认值）。真实租户必须由已鉴权网关注入并签名。
            filterChain.doFilter(request, response);
            return;
        }
        if (!TENANT_PATTERN.matcher(tenantId).matches()) {
            LOG.warn("非法租户ID请求被拒绝 tenantId:{}", truncate(tenantId));
            writeReject(response, StaffErrorCode.PARAM_INVALID.getCode(), "非法租户ID");
            return;
        }
        if (!verifySignature(tenantId, request.getHeader(SIGNATURE_HEADER))) {
            LOG.warn("租户头签名校验失败，请求被拒绝 tenantId:{}", truncate(tenantId));
            writeReject(response, StaffErrorCode.PARAM_INVALID.getCode(), "租户头签名校验失败");
            return;
        }
        StaffContextHolder.setTenantId(tenantId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            StaffContextHolder.clear();
        }
    }

    private boolean verifySignature(String tenantId, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);
            byte[] expected = mac.doFinal(tenantId.getBytes(StandardCharsets.UTF_8));
            byte[] provided = HexFormat.of().parseHex(signature);
            return MessageDigest.isEqual(expected, provided);
        } catch (Exception ex) {
            LOG.warn("租户签名解析失败", ex);
            return false;
        }
    }

    private void writeReject(HttpServletResponse response, String errorCode, String msg) throws IOException {
        // 出参对齐 ApiResponse：code 为 Integer，业务错误走 errorCode 字段，避免错误码通道不一致（P2-5）
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":1,\"errorCode\":\"" + errorCode + "\",\"msg\":\"" + msg + "\"}");
    }

    private String truncate(String value) {
        return value.length() > 64 ? value.substring(0, 64) : value;
    }
}