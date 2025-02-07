package br.com.wedocode.shopping.view.gwt;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.util.Reference;
import elemental2.dom.DomGlobal;

public class ScheduledExecutorGwt implements ScheduledExecutor {

    private static Logger LOG = LoggerFactory.getLogger(ScheduledExecutorGwt.class);

    private static final ScheduledExecutorGwt INSTANCE = new ScheduledExecutorGwt();

    public static ScheduledExecutorGwt get() {
        return INSTANCE;
    }

    private ScheduledExecutorGwt() {
        // NOOP
    }

    @Override
    public Registration execute(ThrowingRunnable command) {
        var registration = new Reference<>(Registration.NOOP);

        var timeoutId = DomGlobal.setTimeout(args -> {
            try {
                registration.set(Registration.NOOP);
                command.runThrows();
            } catch (Throwable caught) {
                LOG.error("Executing", caught);
            }
        });

        registration.set(() -> {
            registration.set(Registration.NOOP);
            DomGlobal.clearTimeout(timeoutId);
        });

        return () -> registration.get().remove();
    }

    @Override
    public Registration schedule(ThrowingRunnable command, Duration delay) {
        var registration = new Reference<>(Registration.NOOP);

        var timeoutId = DomGlobal.setTimeout(args -> {
            try {
                registration.set(Registration.NOOP);
                command.runThrows();
            } catch (Throwable caught) {
                LOG.error("Executing scheduled", caught);
            }
        }, delay.toMillis());

        registration.set(() -> {
            registration.set(Registration.NOOP);
            DomGlobal.clearTimeout(timeoutId);
        });

        return () -> registration.get().remove();
    }

    @Override
    public Registration scheduleAtFixedRate(ThrowingRunnable command, Duration initialDelay, Duration period) {
        // Browser does not offer this option, so the nearest solution is to use scheduleWithFixedDelay
        return scheduleWithFixedDelay(command, initialDelay, period);
    }

    @Override
    public Registration scheduleWithFixedDelay(ThrowingRunnable command, Duration initialDelay, Duration delay) {
        var registration = new Reference<>(Registration.NOOP);

        var intervalCb = (DomGlobal.SetIntervalCallbackFn) args -> {
            try {
                command.runThrows();
            } catch (Throwable caught) {
                LOG.error("Executing scheduled command with fixed delay", caught);
            }
        };

        var timeoutId = DomGlobal.setTimeout(args -> {
            try {
                registration.set(Registration.NOOP);
                command.runThrows();

                var intervalId = DomGlobal.setInterval(intervalCb, delay.toMillis());

                registration.set(() -> {
                    registration.set(Registration.NOOP);
                    DomGlobal.clearInterval(intervalId);
                });
            } catch (Throwable caught) {
                LOG.error("Executing scheduled command at first interaction", caught);
            }
        }, initialDelay.toMillis());

        registration.set(() -> {
            registration.set(Registration.NOOP);
            DomGlobal.clearTimeout(timeoutId);
        });

        return () -> registration.get().remove();
    }

}
