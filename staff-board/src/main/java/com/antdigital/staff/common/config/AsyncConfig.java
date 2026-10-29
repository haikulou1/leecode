package com.antdigital.staff.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置：为 {@code @Async("staffImportExecutor")} 提供有界线程池，
 * 避免使用默认无界 SimpleAsyncTaskExecutor 造成线程无界增长（G8.6）。
 * 拒绝策略采用 CallerRunsPolicy，队列满时由提交线程兜底执行，防止导入任务被静默丢弃。
 */
@Configuration
public class AsyncConfig {

    /** 核心线程数：导入解析为 CPU/内存敏感型，核心 2 即可。 */
    private static final int CORE_POOL_SIZE = 2;

    /** 最大线程数：突发并发导入时可扩至 4。 */
    private static final int MAX_POOL_SIZE = 4;

    /** 有界队列容量：超出后由拒绝策略接管，防止无界堆积 OOM。 */
    private static final int QUEUE_CAPACITY = 100;

    @Bean("staffImportExecutor")
    public Executor staffImportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("staff-import-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
