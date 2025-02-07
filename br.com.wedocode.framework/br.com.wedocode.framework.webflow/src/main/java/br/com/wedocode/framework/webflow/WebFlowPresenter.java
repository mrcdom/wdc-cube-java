package br.com.wedocode.framework.webflow;

import br.com.wedocode.framework.commons.util.Promise;

public interface WebFlowPresenter {

    Promise<Boolean> resolveParameters(WebFlowIntent place);

    boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception;

    void publishParameters(WebFlowIntent intent);

    default void commitComputedState() {
        // NOOP
    }

    void release();

}
