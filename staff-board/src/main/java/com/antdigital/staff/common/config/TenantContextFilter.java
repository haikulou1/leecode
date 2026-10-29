package com.antdigital.staff.common.config;

import com.antdigital.staff.common.context.StaffContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 租户上下文过滤器：从请求头读取租户 ID 注入线程上下文，请求结束清理。
 * 生产环境鉴权（登录态 + 垂直/水平权限）由统一网关/登录态拦截器完成，本过滤器只做
 * 租户 ID 的防御性格式校验与上下文承接，禁止裸信任任意租户 ID 字符串。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final Pattern TENANT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !TENANT_PATTERN.matcher(tenantId).matches()) {
            LOG.warn("非法租户ID请求被拒绝 tenantId:{}", truncate(tenantId));
            writeReject(response);
            return;
        }
        StaffContextHolder.setTenantId(tenantId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            StaffContextHolder.clear();
        }
    }

    private void writeReject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"STAFF_001\",\"msg\":\"非法租户ID\"}");
    }

    private String truncate(String value) {
        return value.length() > 64 ? value.substring(0, 64) : value;
    }
}