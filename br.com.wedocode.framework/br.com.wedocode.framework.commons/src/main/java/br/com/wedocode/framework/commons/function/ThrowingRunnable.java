package br.com.wedocode.framework.commons.function;

import br.com.wedocode.framework.commons.util.Rethrow;

public interface ThrowingRunnable extends Runnable {

    static ThrowingRunnable noop() {
        return ThrowingConsts.NOOP;
    }

    @Override
    default void run() {
        try {
            runThrows();
        } catch (final Exception caught) {
            Rethrow.emit(caught);
        }
    }

    void runThrows() throws Exception;

}
