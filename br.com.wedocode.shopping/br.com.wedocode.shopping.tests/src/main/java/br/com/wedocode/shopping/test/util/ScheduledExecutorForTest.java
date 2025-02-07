package br.com.wedocode.shopping.test.util;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;

public interface ScheduledExecutorForTest extends ScheduledExecutor {

    void flush() throws Exception;

    void shutdown();

}
