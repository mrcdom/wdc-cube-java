package br.com.wedocode.framework.webflow;

import br.com.wedocode.framework.commons.util.Promise;

public class WebFlowAbstractPresenter<A extends WebFlowApplication> implements WebFlowPresenter {

    protected final A app;

    protected WebFlowView view;

    public WebFlowAbstractPresenter(A app) {
        this.app = app;
    }

    public A getApp() {
        return app;
    }

    public final WebFlowView getView() {
        return view;
    }

    @Override
    public void release() {
        if (view != null) {
            view.release();
            view = null;
        }
    }

    public void update() {
        if (view != null) {
            view.update();
        }
    }

    @Override
    public Promise<Boolean> resolveParameters(WebFlowIntent intent) {
        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        return true;
    }

    @Override
    public void publishParameters(WebFlowIntent intent) {
        // NOOP
    }

}
