package com.vikim.vnotes.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean("logTaskExecutor")
    public Executor logTaskExecutor(){
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        // 适合非重要日志任务
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);

        // 这样日志中能看到任务去了哪个线程池
        executor.setThreadNamePrefix("vnotes-async-log-");

        // 因为 @Async 会切换线程
        // 所以需要把提交任务的线程的 MDC 复制到工作线程
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 在队列满时直接丢弃任务，不会回到 HTTP 请求线程执行
        // 任务也不会进入相应的 @Async 方法执行，所以不会打印日志（如果有 log.xxx）
        // 未来需要监控丢弃数量时，再换成自定义拒绝处理器即可
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.DiscardPolicy()
        );

        executor.initialize();

        return executor;
    }
}
