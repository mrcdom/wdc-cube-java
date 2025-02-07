package br.com.wedocode.shopping.test.mock;

import java.util.HashMap;
import java.util.Map;

import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.test.mock.viewimpl.CartViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.LoginViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.ProductViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.ReceiptViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.RestrictedViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.RootViewMock;

public class ShoppingApplicationMock extends ShoppingApplication {

    static {
        RootPresenter.createView = p -> new RootViewMock((ShoppingApplicationMock) p.getApp(), p);
        LoginPresenter.createView = p -> new LoginViewMock((ShoppingApplicationMock) p.getApp(), p);
        RestrictedPresenter.createView = p -> new RestrictedViewMock((ShoppingApplicationMock) p.getApp(), p);
        CartPresenter.createView = p -> new CartViewMock((ShoppingApplicationMock) p.getApp(), p);
        ProductPresenter.createView = p -> new ProductViewMock((ShoppingApplicationMock) p.getApp(), p);
        ReceiptPresenter.createView = p -> new ReceiptViewMock((ShoppingApplicationMock) p.getApp(), p);
    }

    private final Map<String, Object> attributes = new HashMap<>();

    public ShoppingApplicationMock() {
        // NOOP
    }

    @Override
    public void release() {
        super.release();
    }

    public RootViewMock getRootView() {
        var rootPresenter = this.getRootPresenter();
        if (rootPresenter != null && rootPresenter.getView() instanceof RootViewMock) {
            return (RootViewMock) rootPresenter.getView();
        } else {
            return null;
        }
    }

    @Override
    public Object setAttribute(String name, Object value) {
        return this.attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return this.attributes.remove(name);
    }

    @Override
    public void updateHistory() {
        // NOOP
    }

}