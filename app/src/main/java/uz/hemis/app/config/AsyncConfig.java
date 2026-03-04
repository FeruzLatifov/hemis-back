package uz.hemis.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Audit uchun dedicated thread pool konfiguratsiyasi.
 *
 * <p>Audit eventlar asosiy so'rov threadni bloklamasligi uchun
 * alohida executor ishlatiladi. Queue to'lganda CallerRunsPolicy —
 * event yo'qolmaydi, caller threadda bajariladi.</p>
 *
 * <p>AsyncConfigurer orqali uncaught exception handler ham qo'shilgan —
 * async xatolar silent swallow bo'lmaydi.</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AsyncConfig implements AsyncConfigurer {

    @Bean("auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async audit error in {}: {}", method.getName(), ex.getMessage(), ex);
    }
}
