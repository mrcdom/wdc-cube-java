package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.commons.util.Rethrow;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;
import jakarta.servlet.http.HttpSession;

public class ShoppingApplicationImpl extends ShoppingApplication {

    private static Logger LOG = LoggerFactory.getLogger(ShoppingApplicationImpl.class);

    /*
     * View Factory
     */

    static {
        RootPresenter.createView = p -> new RootViewImpl((ShoppingApplicationImpl) p.getApp(), p);
        LoginPresenter.createView = p -> new LoginViewImpl((ShoppingApplicationImpl) p.getApp(), p);
        RestrictedPresenter.createView = p -> new RestrictedViewImpl((ShoppingApplicationImpl) p.getApp(), p);
        CartPresenter.createView = p -> new CartViewImpl((ShoppingApplicationImpl) p.getApp(), p);
        ProductPresenter.createView = p -> new ProductViewImpl((ShoppingApplicationImpl) p.getApp(), p);
        ReceiptPresenter.createView = p -> new ReceiptViewImpl((ShoppingApplicationImpl) p.getApp(), p);
    }

    /*
     * Instance Holder
     */

    /**
     * Application instances are kept out of HttpSession because theirs states are recovered based on place string
     */
    private static ConcurrentHashMap<String, ShoppingApplicationImpl> instanceMap = new ConcurrentHashMap<>();

    private static ShoppingApplicationImpl createApp(HttpSession session, String placeStr) {
        var app = new ShoppingApplicationImpl(session, () -> instanceMap.remove(session.getId()));
        try {
            app.go(placeStr);
        } catch (Exception caught) {
            app.release();
            return Rethrow.emit(caught);
        }
        return app;
    }

    public static ShoppingApplicationImpl get(HttpSession session) {
        return instanceMap.get(session.getId());
    }

    public static ShoppingApplicationImpl getOrCreate(HttpSession session, String placeStr) {
        return instanceMap.computeIfAbsent(session.getId(), sessingId -> ShoppingApplicationImpl.createApp(session, placeStr));
    }

    public static ShoppingApplicationImpl remove(HttpSession session) {
        return instanceMap.remove(session.getId());
    }

    /*
     * Instance
     */

    private transient HttpSession httpSession;

    private transient NumberFormat numberFormat;

    private boolean dirtyHistory;

    private Runnable removeInstanceAction;

    public ShoppingApplicationImpl(HttpSession httpSession, Runnable removeInstanceAction) {
        this.httpSession = httpSession;
        this.removeInstanceAction = removeInstanceAction;
    }

    @Override
    public void release() {
        if (this.removeInstanceAction != null) {
            try {
                this.removeInstanceAction.run();
            } catch (Exception caught) {
                LOG.error("Running removeInstanceAction", caught);
            }
            this.removeInstanceAction = null;
        }
        super.release();
    }

    public boolean isHistoryDirty() {
        return this.dirtyHistory;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public NumberFormat formatNumber() {
        if (this.numberFormat == null) {
            this.numberFormat = NumberFormat.getNumberInstance(Locale.ITALY);
        }
        return this.numberFormat;
    }

    @Override
    public Object setAttribute(String name, Object value) {
        final var old = this.httpSession.getAttribute(name);
        this.httpSession.setAttribute(name, value);
        return old;
    }

    @Override
    public Object getAttribute(String name) {
        return this.httpSession.getAttribute(name);
    }

    @Override
    public Object removeAttribute(String name) {
        final var old = this.httpSession.getAttribute(name);
        this.httpSession.removeAttribute(name);
        return old;
    }

    @Override
    public void updateHistory() {
        this.dirtyHistory = true;
    }

    public void doUpdateHistory() {
        this.fragment = this.newPlace().toString();
        this.dirtyHistory = false;
    }

    public void syncState(FormData form, Promise<Boolean> action) {
        var rootPresenter = this.getRootPresenter();
        if (rootPresenter != null && rootPresenter.getView() instanceof RootViewImpl) {
            ((RootViewImpl) rootPresenter.getView()).syncState(form, action);
        }
    }

    public void render(PrintWriter wr) {
        var rootPresenter = this.getRootPresenter();
        if (rootPresenter != null && rootPresenter.getView() instanceof RootViewImpl) {
            ((RootViewImpl) rootPresenter.getView()).render(wr);
        }
    }

}