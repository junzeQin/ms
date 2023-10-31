package io.metersphere.autoconfigure;

import com.google.common.base.Throwables;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

@EnableAsync(proxyTargetClass = true)
@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 获取当前机器的核数, 不一定准确 请根据实际场景 CPU密集 || IO 密集
     */
    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    /**
     * 线程池名前缀
     */
    private static final String ASYNC_THREAD_NAME_PREFIX = "Async-Executor-";

    @Value("${thread.pool.corePoolSize:}")
    private Optional<Integer> corePoolSize;

    @Value("${thread.pool.maxPoolSize:}")
    private Optional<Integer> maxPoolSize;

    @Value("${thread.pool.queueCapacity:}")
    private Optional<Integer> queueCapacity;

    @Value("${thread.pool.awaitTerminationSeconds:}")
    private Optional<Integer> awaitTerminationSeconds;

    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程大小 默认区 CPU 数量
        taskExecutor.setCorePoolSize(corePoolSize.orElse(CPU_NUM));
        // 最大线程大小 默认区 CPU * 2 数量
        taskExecutor.setMaxPoolSize(maxPoolSize.orElse(CPU_NUM * 2));
        // 队列最大容量
        taskExecutor.setQueueCapacity(queueCapacity.orElse(300));
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds.orElse(60));
        taskExecutor.setThreadNamePrefix(ASYNC_THREAD_NAME_PREFIX);
        taskExecutor.initialize();

        return taskExecutor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SpringAsyncExceptionHandler();
    }

    static class SpringAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
            log.error("Exception occurs in async method[{}],error {}", method.getName(), Throwables.getStackTraceAsString(throwable));
        }

    }

}
