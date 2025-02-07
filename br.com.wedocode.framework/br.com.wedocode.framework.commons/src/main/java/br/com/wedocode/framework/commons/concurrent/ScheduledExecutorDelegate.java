package br.com.wedocode.framework.commons.concurrent;

import java.time.Duration;

import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;

public class ScheduledExecutorDelegate implements ScheduledExecutor {

    protected ScheduledExecutor impl;

    public ScheduledExecutorDelegate() {
        impl = ScheduledExecutorUnavailable.INSTANCE;
    }

    public ScheduledExecutorDelegate(ScheduledExecutor impl) {
        this.impl = impl != null ? impl : ScheduledExecutorUnavailable.INSTANCE;
    }

    public void setImpl(ScheduledExecutor impl) {
        this.impl = impl != null ? impl : ScheduledExecutorUnavailable.INSTANCE;
    }

    @Override
    public Registration execute(ThrowingRunnable command) {
        return impl.execute(command);
    }

    @Override
    public Registration schedule(ThrowingRunnable command, Duration delay) {
        return impl.schedule(command, delay);
    }

    @Override
    public Registration scheduleAtFixedRate(ThrowingRunnable command, Duration initialDelay, Duration period) {
        return impl.scheduleAtFixedRate(command, initialDelay, period);
    }

    @Override
    public Registration scheduleWithFixedDelay(ThrowingRunnable command, Duration initialDelay, Duration delay) {
        return impl.scheduleWithFixedDelay(command, initialDelay, delay);
    }

    private static class ScheduledExecutorUnavailable implements ScheduledExecutor {

        private static ScheduledExecutorUnavailable INSTANCE = new ScheduledExecutorUnavailable();

        private ScheduledExecutorUnavailable() {
            // NOOP
        }

        private RuntimeException newUnavailableException() {
            return new RuntimeException("Unavailable");
        }

        @Override
        public Registration execute(ThrowingRunnable command) {
            throw newUnavailableException();
        }

        @Override
        public Registration schedule(ThrowingRunnable command, Duration delay) {
            throw newUnavailableException();
        }

        @Override
        public Registration scheduleAtFixedRate(ThrowingRunnable command, Duration initialDelay, Duration period) {
            throw newUnavailableException();
        }

        @Override
        public Registration scheduleWithFixedDelay(ThrowingRunnable command, Duration initialDelay, Duration delay) {
            throw newUnavailableException();
        }

    }

}
