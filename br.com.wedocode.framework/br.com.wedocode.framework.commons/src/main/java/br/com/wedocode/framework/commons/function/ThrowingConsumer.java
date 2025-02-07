package br.com.wedocode.framework.commons.function;

import java.util.Objects;
import java.util.function.Consumer;

import br.com.wedocode.framework.commons.util.Rethrow;

public interface ThrowingConsumer<T> extends Consumer<T> {

    @SuppressWarnings("unchecked")
    static <P> ThrowingConsumer<P> noop() {
        return (ThrowingConsumer<P>) ThrowingConsts.NOOP_CONSUMER;
    }

    @Override
    default void accept(final T t) {
        try {
            this.acceptThrows(t);
        } catch (final Exception caught) {
            Rethrow.emit(caught);
        }
    }

    void acceptThrows(T t) throws Exception;

    default ThrowingConsumer<T> andThen(final ThrowingConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (final T t) -> {
            this.accept(t);
            after.accept(t);
        };
    }

}
