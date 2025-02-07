package br.com.wedocode.shopping.test.mock.viewimpl;

import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public abstract class AbstractViewMock<P extends WebFlowPresenter> implements WebFlowView {

    /*
     * Fields
     */

    public boolean released;

    public final ShoppingApplicationMock app;

    public final P presenter;

    /*
     * Constructor
     */

    public AbstractViewMock(ShoppingApplicationMock app, P presenter) {
        this.app = app;
        this.presenter = presenter;
    }

    /*
     * API
     */

    @Override
    public void release() {
        this.released = true;
    }

    @Override
    public void update() {

    }

}
