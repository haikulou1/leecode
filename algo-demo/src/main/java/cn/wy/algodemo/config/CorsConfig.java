package cn.wy.algodemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置。
 *
 * <p>静态前端（haikulou1.github.io）与 Java 后端分属不同源，必然跨域。
 * 对 {@code /api/**} 采用显式白名单策略：仅放开生产站点与本地开发源，
 * 方法限定 GET/POST/OPTIONS，关闭 allowCredentials，预检缓存 3600s。</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "https://haikulou1.github.io",
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
