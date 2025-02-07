package br.com.wedocode.framework.commons.concurrent;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;

public class ScheduledExecutorForTestSyncDelayed implements ScheduledExecutorForTest {

    private int sequenceGenerator;

    private int lastExecutedSequence;

    private Map<Integer, ThrowingRunnable> commandMap;

    public ScheduledExecutorForTestSyncDelayed() {
        this.commandMap = new ConcurrentHashMap<Integer, ThrowingRunnable>();
    }

    @Override
    public Registration execute(ThrowingRunnable command) {
        var sequenceId = Integer.valueOf(sequenceGenerator++);
        commandMap.put(sequenceId, command);
        return () -> {
            commandMap.remove(sequenceId);
        };
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
        for (int i = lastExecutedSequence; i < sequenceGenerator; i++) {
            lastExecutedSequence = i + 1;
            var cmd = commandMap.remove(i);
            if (cmd != null) {
                cmd.run();
            }

        }
    }

    @Override
    public void shutdown() {
        commandMap.clear();
    }

}
