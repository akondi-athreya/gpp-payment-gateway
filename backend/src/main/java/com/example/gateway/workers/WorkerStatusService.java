package com.example.gateway.workers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class WorkerStatusService {

    private final TaskExecutor taskExecutor;

    public WorkerStatusService(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public boolean isRunning() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            taskExecutor.execute(latch::countDown);
            return latch.await(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
}
