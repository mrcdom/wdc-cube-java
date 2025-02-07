package br.com.wedocode.framework.commons.function;

import java.util.Objects;
import java.util.function.BiConsumer;

import br.com.wedocode.framework.commons.util.Rethrow;

public interface ThrowingBiConsumer<T, U> extends BiConsumer<T, U> {

    @SuppressWarnings("unchecked")
    static <P0, P1> ThrowingBiConsumer<P0, P1> noop() {
        return (ThrowingBiConsumer<P0, P1>) ThrowingConsts.NOOP_BICONSUMER;
    }

    @Override
    default void accept(final T t, final U u) {
        try {
            this.acceptThrows(t, u);
        } catch (final Exception caught) {
            Rethrow.emit(caught);
        }
    }

    void acceptThrows(T t, U u) throws Exception;

    default ThrowingBiConsumer<T, U> andThen(final ThrowingBiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            this.accept(l, r);
            after.accept(l, r);
        };
    }

}
