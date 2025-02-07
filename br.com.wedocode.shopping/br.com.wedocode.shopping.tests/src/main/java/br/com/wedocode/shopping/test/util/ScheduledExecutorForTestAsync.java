package br.com.wedocode.shopping.test.util;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;

public class ScheduledExecutorForTestAsync implements ScheduledExecutorForTest {

    private ScheduledExecutorService service;

    private AtomicInteger counter;

    private volatile boolean running;

    public ScheduledExecutorForTestAsync(int corePoolSize) {
        this.counter = new AtomicInteger(0);
        this.running = true;
        this.service = Executors.newScheduledThreadPool(corePoolSize);
    }

    @Override
    public void shutdown() {
        this.running = false;
        this.service.shutdown();
    }

    @Override
    public Registration execute(ThrowingRunnable command) {
        this.counter.incrementAndGet();
        var future = this.service.schedule(() -> {
            try {
                command.run();
            } finally {
                this.counter.decrementAndGet();
            }
        }, 0, TimeUnit.MILLISECONDS);

        return () -> {
            this.counter.decrementAndGet();
            future.cancel(true);
        };
    }

    @Override
    public Registration schedule(ThrowingRunnable command, Duration delay) {
        this.counter.incrementAndGet();
        var future = this.service.schedule(() -> {
            try {
                command.run();
            } finally {
                this.counter.decrementAndGet();
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);

        return () -> {
            this.counter.decrementAndGet();
            future.cancel(true);
        };
    }

    @Override
    public Registration scheduleAtFixedRate(ThrowingRunnable command, Duration initialDelay, Duration period) {
        this.counter.incrementAndGet();
        var future = this.service.scheduleAtFixedRate(() -> {
            try {
                command.run();
            } finally {
                this.counter.decrementAndGet();
            }
        }, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);

        return () -> {
            this.counter.decrementAndGet();
            future.cancel(true);
        };
    }

    @Override
    public Registration scheduleWithFixedDelay(ThrowingRunnable command, Duration initialDelay, Duration delay) {
        this.counter.incrementAndGet();
        var future = this.service.scheduleWithFixedDelay(() -> {
            try {
                command.run();
            } finally {
                this.counter.decrementAndGet();
            }
        }, initialDelay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);

        return () -> {
            this.counter.decrementAndGet();
            future.cancel(true);
        };
    }

    @Override
    public void flush() {
        while (this.running && this.counter.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException caught) {
                // NOOP
            }
        }
    }

}
