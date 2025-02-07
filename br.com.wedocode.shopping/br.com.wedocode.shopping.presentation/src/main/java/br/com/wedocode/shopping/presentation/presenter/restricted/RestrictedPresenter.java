package br.com.wedocode.shopping.presentation.presenter.restricted;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowAbstractPresenter;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.framework.webflow.WebFlowViewSlot;
import br.com.wedocode.shopping.presentation.PlaceAttributes;
import br.com.wedocode.shopping.presentation.PlaceParameters;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.exception.ProductNotFoundException;
import br.com.wedocode.shopping.presentation.exception.PurchaseNotFoundException;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;

public class RestrictedPresenter extends WebFlowAbstractPresenter<ShoppingApplication> {

    private static Logger LOG = LoggerFactory.getLogger(RestrictedPresenter.class);

    public class RestrictedState {

        public WebFlowView contentView;

        public String nickName;

        public List<PurchaseInfo> purchases;

        public List<ProductItem> products;

        public int cartItemCount;

        public int errorCode;

        public String errorMessage;

    }

    public static Function<RestrictedPresenter, WebFlowView> createView;

    /*
     * Fields
     */

    private final RestrictedState state = new RestrictedState();

    private final ShoppingDAO dao;

    private final WebFlowViewSlot contentSlot;

    private WebFlowViewSlot ownerSlot;

    private CartManager cart;

    private ThrowingRunnable onCartCommitListenerRemover;
    private ThrowingRunnable onCartChangeListenerRemover;

    /*
     * Life cycle
     */

    public RestrictedPresenter(ShoppingApplication app) {
        super(app);
        this.dao = ShoppingContext.getDAO();
        this.contentSlot = this::setContentView;
        this.onCartCommitListenerRemover = ThrowingRunnable.noop();
        this.onCartChangeListenerRemover = ThrowingRunnable.noop();
    }

    @Override
    public void release() {
        this.state.contentView = null;

        this.app.setCart(null);

        this.onCartCommitListenerRemover.run();
        this.onCartCommitListenerRemover = ThrowingRunnable.noop();

        this.onCartChangeListenerRemover.run();
        this.onCartChangeListenerRemover = ThrowingRunnable.noop();

        if (this.view != null) {
            this.view.release();
            this.view = null;
        }
    }

    public RestrictedState getState() {
        return state;
    }

    /*
     * Webflow
     */

    @Override
    public Promise<Boolean> resolveParameters(WebFlowIntent place) {
        if (this.state.purchases == null || this.state.products == null) {
            return this.loadOrReloadPurchesesAndProducts();
        }
        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        if (this.app.getSubject() == null) {
            Routes.login(this.app, intent);
            return false;
        }

        if (initialization || this.view == null) {
            this.ownerSlot = intent.getViewSlot(PlaceAttributes.SLOT_OWNER);
            this.view = createView.apply(this);

            this.state.nickName = this.app.getSubject().getNickName();

            this.cart = new CartManager(app, this.dao);
            this.onCartCommitListenerRemover = this.cart.addCommitListener(this::onCartCommited);
            this.onCartChangeListenerRemover = this.cart.addChangeListener(this::onCartChanged);
            this.app.setCart(this.cart);
        }

        if (this.ownerSlot == null) {
            return false;
        }

        this.ownerSlot.setView(this.view);

        if (deepest) {
            this.setContentView(null);
        } else {
            intent.setViewSlot(PlaceAttributes.SLOT_OWNER, this.contentSlot);
        }

        return true;
    }

    @Override
    public void publishParameters(WebFlowIntent intent) {
        // NOOP
    }

    @Override
    public void commitComputedState() {
        var newCartItemCount = this.cart != null ? this.cart.getSize() : 0;
        if (this.state.cartItemCount != newCartItemCount) {
            this.state.cartItemCount = newCartItemCount;
            this.update();
        }
    }

    /*
     * API
     */

    private Promise<Boolean> onCartCommited() {
        return this.loadOrReloadPurchesesAndProducts()

                // OnFailure
                .catch_(caught -> {
                    LOG.error("It was not possible refresh cart data", caught);
                    return Promise.resolve(Boolean.FALSE);
                });
    }

    private void onCartChanged() {
        this.state.cartItemCount = this.cart.getSize();
        this.update();
    }

    public Promise<Boolean> onOpenReceipt(Long purchaseId) {
        var unexpectedError = app.unexpectedError(caught -> {
            if (caught instanceof PurchaseNotFoundException) {
                this.alertPurchaseNotFound();
                LOG.warn(this.state.errorMessage + ": purchaseId=" + purchaseId);
            } else {
                this.app.alertUnexpectedError(LOG, "Trying to go to receipt place to show purchaseId=" + purchaseId,
                        caught);
            }
        });

        try {
            if (purchaseId == null) {
                this.alertPurchaseIdRequired();
                LOG.warn("onOpenReceipt: " + this.state.errorMessage);
                return Promise.resolve(Boolean.FALSE);
            }

            var place = this.app.newPlace();
            place.setParameter(PlaceParameters.PURCHASE_ID, purchaseId);
            return Routes.receipt(this.app, place).catch_(unexpectedError);
        } catch (Throwable caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    public Promise<Boolean> onOpenProduct(Long productId) {
        var unexpectedError = app.unexpectedError(caught -> {
            if (caught instanceof ProductNotFoundException) {
                this.alertProductNotFound();
                LOG.warn(this.state.errorMessage + ": productId=" + productId);
            } else {
                this.app.alertUnexpectedError(LOG, "Trying to go to product place to show productId=" + productId,
                        caught);
            }
        });

        try {
            if (productId == null) {
                this.alertProductIdRequired();
                LOG.warn("onOpenProduct: " + this.state.errorMessage);
                return Promise.resolve(Boolean.FALSE);
            }

            var place = this.app.newPlace();
            place.setParameter(PlaceParameters.PRODUCT_ID, productId);
            return Routes.product(this.app, place).catch_(unexpectedError);
        } catch (Throwable caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    public Promise<Boolean> onOpenCart() {
        var unexpectedError = app.unexpectedError(caught -> {
            this.app.alertUnexpectedError(LOG, "Trying to go to cart place", caught);
        });

        try {
            return Routes.cart(this.app).catch_(unexpectedError);
        } catch (Throwable caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    public Promise<Boolean> onExit() {
        var unexpectedError = app.unexpectedError(caught -> {
            this.app.alertUnexpectedError(LOG, "Trying to go to login place", caught);
        });

        try {
            this.cart.clear();
            this.app.setSubject(null);
            this.setContentView(null);

            return Routes.login(this.app).catch_(unexpectedError);
        } catch (Throwable caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    /*
     * Messages
     */

    private void alertProductNotFound() {
        this.state.errorCode = 3;
        this.state.errorMessage = "Código do produto não localizado.";
        this.update();
    }

    private void alertPurchaseNotFound() {
        this.state.errorCode = 5;
        this.state.errorMessage = "Código do recibo não localizado.";
        this.update();
    }

    private void alertProductIdRequired() {
        this.state.errorCode = 6;
        this.state.errorMessage = "Código do produto é um argumento obrigatório.";
        this.update();
    }

    private void alertPurchaseIdRequired() {
        this.state.errorCode = 7;
        this.state.errorMessage = "Código do recibo é um argumento obrigatório.";
        this.update();
    }

    /*
     * Others
     */

    private void setContentView(WebFlowView view) {
        if (this.state.contentView != view) {
            this.state.contentView = view;
            this.update();
        }
    }

    private Promise<Boolean> loadOrReloadPurchesesAndProducts() {
        if (this.app.getSubject() == null) {
            return Promise.resolve(Boolean.FALSE);
        }

        return this.dao.loadPurchases(this.app.getSubject().getId())
                // OnSuccess loading purchases
                .then(purchases -> {
                    synchronized (app) {
                        this.state.purchases = purchases;
                        this.update();
                    }
                    return this.dao.loadProducts(false);
                })

                // OnSuccess loading products
                .then(products -> {
                    synchronized (app) {
                        this.state.products = products;
                        this.state.cartItemCount = this.cart != null ? this.cart.getSize() : 0;
                        this.update();
                    }
                    return Promise.resolve(Boolean.TRUE);
                });
    }

}
