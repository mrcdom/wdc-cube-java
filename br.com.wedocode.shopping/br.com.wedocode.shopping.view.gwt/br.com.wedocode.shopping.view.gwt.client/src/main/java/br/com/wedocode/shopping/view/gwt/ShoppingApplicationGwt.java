package br.com.wedocode.shopping.view.gwt;

import static elemental2.dom.DomGlobal.window;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.storage.client.Storage;

import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.gwt.impl.CartViewGwt;
import br.com.wedocode.shopping.view.gwt.impl.LoginViewGwt;
import br.com.wedocode.shopping.view.gwt.impl.ProductViewGwt;
import br.com.wedocode.shopping.view.gwt.impl.ReceiptViewGwt;
import br.com.wedocode.shopping.view.gwt.impl.RestrictedViewGwt;
import br.com.wedocode.shopping.view.gwt.impl.RootViewGwt;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;

public class ShoppingApplicationGwt extends ShoppingApplication {

    private static Logger LOG = LoggerFactory.getLogger(ShoppingApplicationGwt.class);

    static {
        RootPresenter.createView = p -> new RootViewGwt((ShoppingApplicationGwt) p.getApp(), p);
        LoginPresenter.createView = p -> new LoginViewGwt((ShoppingApplicationGwt) p.getApp(), p);
        RestrictedPresenter.createView = p -> new RestrictedViewGwt((ShoppingApplicationGwt) p.getApp(), p);
        CartPresenter.createView = p -> new CartViewGwt((ShoppingApplicationGwt) p.getApp(), p);
        ProductPresenter.createView = p -> new ProductViewGwt((ShoppingApplicationGwt) p.getApp(), p);
        ReceiptPresenter.createView = p -> new ReceiptViewGwt((ShoppingApplicationGwt) p.getApp(), p);
    }

    private Storage storage;

    private BiFunction<String, Object, Object> fnSetAttribute;

    private Function<String, Object> fnGetAttribute;

    private Function<String, Object> fnRemoveAttribute;

    private HTMLDivElement mainDiv;

    private double updateHistoryHandler;

    private DomGlobal.SetTimeoutCallbackFn doUpdateHistoryCallback;

    private Map<String, Runnable> pendingUpdateMap;

    private double updateIntervalId;

    public ShoppingApplicationGwt() {
        this.updateHistoryHandler = 0.0;
        this.doUpdateHistoryCallback = this::doUpdateHistory;
        this.pendingUpdateMap = new LinkedHashMap<>();
        this.initAttributeHandlers();
    }

    @Override
    public void release() {
        DomGlobal.clearInterval(this.updateIntervalId);
        this.updateIntervalId = 0.0;
        super.release();
    }

    private void initAttributeHandlers() {
        var map = new HashMap<>();
        this.storage = Storage.getLocalStorageIfSupported();
        if (this.storage != null) {
            this.fnGetAttribute = name -> {
                var value = map.get(name);
                if (value == null) {
                    return this.storage.getItem(name);
                } else {
                    return value;
                }
            };

            this.fnSetAttribute = (name, value) -> {
                var old = map.put(name, value);
                if (value instanceof String) {
                    this.storage.setItem(name, (String) value);
                } else if (value == null) {
                    this.storage.removeItem(name);
                }
                return old;
            };

            this.fnRemoveAttribute = name -> {
                var old = (Object) this.storage.getItem(name);
                if (old != null) {
                    this.storage.removeItem(name);
                    map.remove(name);
                } else {
                    old = map.remove(name);
                }
                return old;
            };
        } else {
            this.fnGetAttribute = map::get;
            this.fnSetAttribute = map::put;
            this.fnRemoveAttribute = map::remove;
        }
    }

    @Override
    public Object setAttribute(String name, Object value) {
        return this.fnSetAttribute.apply(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return this.fnGetAttribute.apply(name);
    }

    @Override
    public Object removeAttribute(String name) {
        return this.fnRemoveAttribute.apply(name);
    }

    public String getCurrentPlaceStr() {
        var hash = window.location.hash;
        if (hash != null && hash.length() > 0 && hash.charAt(0) == '#') {
            return hash.substring(1);
        } else {
            return null;
        }
    }

    public Object onHashChanged(Event evt) {
        this.go(getCurrentPlaceStr()).catch_(caught -> {
            LOG.warn("onHashChanged", caught);
            return null;
        });
        return null;
    }

    @Override
    public void updateHistory() {
        if (this.updateHistoryHandler != 0.0) {
            DomGlobal.clearTimeout(this.updateHistoryHandler);
            this.updateHistoryHandler = 0.0;
        }
        this.updateHistoryHandler = DomGlobal.setTimeout(this.doUpdateHistoryCallback, 16);
    }

    protected void doUpdateHistory(Object[] args) {
        this.updateHistoryHandler = 0.0;
        window.location.hash = "#" + this.newPlace().toString();
    }

    public HTMLDivElement getMainDiv() {
        return this.mainDiv;
    }

    public void setMainDiv(HTMLDivElement mainDiv) {
        this.mainDiv = mainDiv;
    }

    public RootViewGwt getRootView() {
        var rootPresenter = this.getRootPresenter();
        if (rootPresenter != null && rootPresenter.getView() instanceof RootViewGwt) {
            return (RootViewGwt) rootPresenter.getView();
        } else {
            return null;
        }
    }

    public void pushUpdate(String viewId, Runnable updateAction) {
        this.pendingUpdateMap.put(viewId, updateAction);
    }

    public void removeUpdate(String viewId) {
        this.pendingUpdateMap.remove(viewId);
    }

    public void start() {
        this.updateIntervalId = DomGlobal.setInterval(this::runUpdates, 16);
        this.onHashChanged(null);
    }

    private void runUpdates(Object[] args) {
        if (this.pendingUpdateMap.size() > 0) {
            this.commitComputedState();

            var it = this.pendingUpdateMap.values().iterator();
            while (it.hasNext()) {
                var task = it.next();
                it.remove();
                task.run();
            }
        }
    }

}
