package br.com.wedocode.shopping.presentation;

import org.slf4j.Logger;

import br.com.wedocode.framework.commons.function.ThrowingConsumer;
import br.com.wedocode.framework.commons.function.ThrowingFunction;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowApplication;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.framework.webflow.WebFlowPlace;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartManager;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;

public abstract class ShoppingApplication extends WebFlowApplication {

    protected Subject subject;

    protected CartManager cart;

    public ShoppingApplication() {
        super();
    }

    /*
     * Getters and Setters
     */

    public WebFlowPlace getRootPlace() {
        return Routes.Place.ROOT;
    }

    public RootPresenter getRootPresenter() {
        return (RootPresenter) this.presenterMap.get(Routes.Place.ROOT.getId());
    }

    public Subject getSubject() {
        return this.subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public CartManager getCart() {
        return this.cart;
    }

    public CartManager setCart(CartManager cart) {
        var old = this.cart;
        this.cart = cart;
        return old;
    }

    /*
     * Extentions
     */

    @SuppressWarnings("unchecked")
    public Navigation<ShoppingApplication> navigate() {
        return super.navigate();
    }

    /*
     * API
     */

    public void alertUnexpectedError(Logger logger, String message, Throwable e) {
        var rootPresenter = this.getRootPresenter();
        if (rootPresenter != null) {
            rootPresenter.alertUnexpectedError(logger, message, e);
        }
    }

    public ThrowingFunction<Throwable, Promise<Boolean>> unexpectedError(ThrowingConsumer<Throwable> action) {
        return (caught) -> {
            synchronized (this) {
                action.accept(caught);
                return null;
            }
        };
    }

    public Promise<Boolean> go(String placeStr) {
        try {
            var intent = WebFlowIntent.parse(placeStr);
            return this.go(intent);
        } catch (Exception caught) {
            return Promise.reject(caught);
        }
    }

    public Promise<Boolean> go(WebFlowIntent intent) {
        try {
            var promise = ShoppingContext.Internals.go(this, intent);
            if (promise == null) {
                if (this.getSubject() != null) {
                    promise = Routes.restricted(this);
                } else {
                    promise = Routes.login(this);
                }

                if (promise == null) {
                    promise = Promise.resolve(Boolean.FALSE);
                }
            }
            return promise;
        } catch (Exception caught) {
            return Promise.reject(caught);
        }
    }

}
