package br.com.wedocode.shopping.view.gwt.util;

import org.slf4j.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;

import br.com.wedocode.framework.commons.function.ThrowingConsumer;

public class AsyncCallbackHelper<T> implements AsyncCallback<T> {

    private Logger logger;
    private ThrowingConsumer<T> resolve;
    private ThrowingConsumer<Throwable> reject;

    public AsyncCallbackHelper(Logger logger, ThrowingConsumer<T> resolve, ThrowingConsumer<Throwable> reject) {
        this.logger = logger;
        this.resolve = resolve;
        this.reject = reject;
    }

    @Override
    public void onFailure(Throwable caught) {
        try {
            this.reject.accept(caught);
        } catch (Throwable cause) {
            this.logger.error("Unexpected error", cause);
        }
    }

    @Override
    public void onSuccess(T result) {
        try {
            this.resolve.acceptThrows(result);
        } catch (Exception caught) {
            onFailure(caught);
        }
    }

}