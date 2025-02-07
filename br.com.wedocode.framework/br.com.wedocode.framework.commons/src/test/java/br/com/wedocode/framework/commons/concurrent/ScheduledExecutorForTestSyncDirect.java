package br.com.wedocode.framework.commons.concurrent;

import java.time.Duration;

import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;

public class ScheduledExecutorForTestSyncDirect implements ScheduledExecutorForTest {

    private static final Registration NOOP_REGISTRATION = () -> {
        // NOOP
    };

    public ScheduledExecutorForTestSyncDirect() {
    }

    @Override
    public Registration execute(ThrowingRunnable command) {
        command.run();
        return NOOP_REGISTRATION;
    }

    @Override
    public Registration schedule(ThrowingRunnable command, Duration delay) {
        return this.execute(command);
    }

    @Override
    public Registration scheduleAtFixedRate(ThrowingRunnable command, Duration initialDelay, Duration period) {
        return this.execute(command);
    }

    @Override
    public Registration scheduleWithFixedDelay(ThrowingRunnable command, Duration initialDelay, Duration delay) {
        return this.execute(command);
    }

    @Override
    public void flush() {
        // NOOP
    }

    @Override
    public void shutdown() {
        // NOOP
    }

}
