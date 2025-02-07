package br.com.wedocode.framework.commons.concurrent;

public interface ScheduledExecutorForTest extends ScheduledExecutor {

    void flush();

    public void shutdown();

}
