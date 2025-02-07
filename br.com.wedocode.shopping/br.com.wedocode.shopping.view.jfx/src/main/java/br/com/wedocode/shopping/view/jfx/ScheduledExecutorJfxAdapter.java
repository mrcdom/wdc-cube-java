package br.com.wedocode.shopping.view.jfx;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.shopping.business.ScheduledExecutorAdapter;
import javafx.application.Platform;

public class ScheduledExecutorJfxAdapter implements ScheduledExecutor {

    private ScheduledExecutorAdapter impl;

    public ScheduledExecutorJfxAdapter(ScheduledExecutorService service) {
        this.impl = new ScheduledExecutorAdapter(service);
    }

    @Override
    public Registration execute(ThrowingRunnable command) {
        return this.impl.execute(() -> Platform.runLater(command));
    }

    @Override
    public Registration schedule(ThrowingRunnable command, Duration delay) {
        return this.impl.schedule(() -> Platform.runLater(command), delay);
    }

    @Override
    public Registration scheduleAtFixedRate(ThrowingRunnable command, Duration initialDelay, Duration period) {
        return this.impl.scheduleAtFixedRate(() -> Platform.runLater(command), initialDelay, period);
    }

    @Override
    public Registration scheduleWithFixedDelay(ThrowingRunnable command, Duration initialDelay, Duration delay) {
        return this.impl.scheduleWithFixedDelay(() -> Platform.runLater(command), initialDelay, delay);
    }

}
