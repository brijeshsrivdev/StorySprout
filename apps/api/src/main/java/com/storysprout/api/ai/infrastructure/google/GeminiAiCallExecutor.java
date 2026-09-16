package com.storysprout.api.ai.infrastructure.google;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.stereotype.Component;

@Component
public class GeminiAiCallExecutor {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public <T> T execute(Callable<T> operation, long timeout, TimeUnit unit) throws TimeoutException {
        Future<T> future = executor.submit(operation);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw ex;
        } catch (InterruptedException ex) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI call interrupted", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("AI call failed", cause);
        }
    }

    @PreDestroy
    void shutdown() throws InterruptedException {
        executor.shutdownNow();
        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
