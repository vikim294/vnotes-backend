    package com.vikim.demo.common;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 提交任务的线程的 MDC
        Map<String, String> submittingThreadContext =
                MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> workerThreadContext =
                    MDC.getCopyOfContextMap();

            try {
                MDC.clear();

                if (submittingThreadContext != null) {
                    // 工作线程执行前设置 MDC
                    MDC.setContextMap(submittingThreadContext);
                }

                // 工作线程执行
                runnable.run();
            } finally {
                MDC.clear();

                if (workerThreadContext != null) {
                    // 恢复工作线程原来的 MDC
                    MDC.setContextMap(workerThreadContext);
                }
            }
        };
    }
}
