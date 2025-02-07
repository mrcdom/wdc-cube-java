package br.com.wedocode.shopping.view.jfx;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.function.Registration;
import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.jfx.impl.CartViewJfx;
import br.com.wedocode.shopping.view.jfx.impl.LoginViewJfx;
import br.com.wedocode.shopping.view.jfx.impl.ProductViewJfx;
import br.com.wedocode.shopping.view.jfx.impl.ReceiptViewJfx;
import br.com.wedocode.shopping.view.jfx.impl.RestrictedViewJfx;
import br.com.wedocode.shopping.view.jfx.impl.RootViewJfx;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

public class ShoppingJfxApplication extends ShoppingApplication {

    static {
        RootPresenter.createView = p -> new RootViewJfx((ShoppingJfxApplication) p.getApp(), p);
        LoginPresenter.createView = p -> new LoginViewJfx((ShoppingJfxApplication) p.getApp(), p);
        RestrictedPresenter.createView = p -> new RestrictedViewJfx((ShoppingJfxApplication) p.getApp(), p);
        CartPresenter.createView = p -> new CartViewJfx((ShoppingJfxApplication) p.getApp(), p);
        ProductPresenter.createView = p -> new ProductViewJfx((ShoppingJfxApplication) p.getApp(), p);
        ReceiptPresenter.createView = p -> new ReceiptViewJfx((ShoppingJfxApplication) p.getApp(), p);
    }

    private ScheduledExecutor executor = ShoppingContext.getExecutor();

    private Map<String, Object> attributes;

    private StackPane rootElement;

    private Registration runUpdatesRegistration;

    private Map<String, Runnable> pendingUpdateMap;

    private AtomicBoolean runLatterNotRequested;

    public ShoppingJfxApplication() {
        this.attributes = new ConcurrentHashMap<>();
        this.pendingUpdateMap = new ConcurrentHashMap<>();
        this.runLatterNotRequested = new AtomicBoolean(true);
        this.rootElement = new StackPane();
        this.rootElement.getStyleClass().add("root");
    }

    @Override
    public void release() {
        if (this.runUpdatesRegistration != null) {
            this.runUpdatesRegistration.remove();
            this.runUpdatesRegistration = null;
        }
        super.release();
    }

    @Override
    public Object setAttribute(String name, Object value) {
        if (name == null) {
            return value;
        }

        if (value == null) {
            return this.attributes.remove(name);
        }

        return this.attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        if (name == null) {
            return null;
        }
        return this.attributes.get(name);
    }

    @Override
    public Object removeAttribute(String name) {
        if (name == null) {
            return null;
        }
        return this.attributes.remove(name);
    }

    public StackPane getRootElement() {
        return this.rootElement;
    }

    @Override
    public void updateHistory() {
        // NOOP
    }

    public void pushUpdate(String viewId, Runnable updateAction) {
        this.pendingUpdateMap.put(viewId, updateAction);

    }

    public void removeUpdate(String viewId) {
        this.pendingUpdateMap.remove(viewId);
    }

    public void start() {
        var frequency = Duration.ofMillis(16);
        this.runUpdatesRegistration = this.executor.scheduleWithFixedDelay(this::runLater, frequency, frequency);
    }

    private void runLater() {
        if (this.runLatterNotRequested.getAndSet(false)) {
            Platform.runLater(this::runUpdates);
        }
    }

    private void runUpdates() {
        try {
            if (this.pendingUpdateMap.size() > 0) {
                this.commitComputedState();

                var it = this.pendingUpdateMap.values().iterator();
                while (it.hasNext()) {
                    var task = it.next();
                    it.remove();
                    task.run();
                }
            }
        } finally {
            this.runLatterNotRequested.set(true);
        }
    }

    public <T extends WebFlowPresenter> T getPresenterByClass(Class<T> cls) {
        for (WebFlowPresenter presenter : this.presenterMap.values()) {
            if (cls.isInstance(presenter)) {
                return cls.cast(presenter);
            }
        }
        return null;
    }

}
