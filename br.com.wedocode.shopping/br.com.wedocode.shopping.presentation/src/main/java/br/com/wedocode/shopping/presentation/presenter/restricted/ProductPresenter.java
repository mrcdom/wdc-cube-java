package br.com.wedocode.shopping.presentation.presenter.restricted;

import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import br.com.wedocode.shopping.presentation.exception.WrongParametersException;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOOfflineException;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;

public class ProductPresenter extends WebFlowAbstractPresenter<ShoppingApplication> {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPresenter.class.getName());

    public static class ProductState {

        public ProductItem product;

        public int errorCode;

        public String errorMessage;

    }

    public static Function<ProductPresenter, WebFlowView> createView;

    /*
     * Fields
     */

    private final ProductState state = new ProductState();

    private final ShoppingDAO dao;

    private WebFlowViewSlot ownerSlot;

    /*
     * Life cycle
     */

    public ProductPresenter(ShoppingApplication app) {
        super(app);
        this.dao = ShoppingContext.getDAO();
    }

    @Override
    public void release() {
        super.release();
    }

    public ProductState getState() {
        return state;
    }

    /*
     * WebFlow
     */

    @Override
    public Promise<Boolean> resolveParameters(WebFlowIntent place) {
        var oldProductId = this.state.product != null ? this.state.product.id : null;

        var newProductId = place.getParameterAsLong(PlaceParameters.PRODUCT_ID, oldProductId);
        if (newProductId == null) {
            throw new AssertionError("Missing PRODUCT_ID");
        }

        if (this.state.product == null || !Objects.equals(newProductId, oldProductId)) {
            return this.loadProductById(newProductId)

                    // OnSuccess
                    .then(product -> {
                        synchronized (app) {
                            this.state.product = product;
                            this.update();
                        }
                        return Promise.resolve(Boolean.TRUE);
                    });
        }

        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        if (initialization) {
            this.ownerSlot = intent.getViewSlot(PlaceAttributes.SLOT_OWNER);

            this.state.errorCode = 0;
            this.state.errorMessage = null;
            if (this.state.product == null) {
                throw new AssertionError("Missing Product");
            }

            this.view = createView.apply(this);
        }

        this.ownerSlot.setView(this.view);

        return true;
    }

    @Override
    public void publishParameters(WebFlowIntent intent) {
        if (this.state.product != null) {
            intent.setParameter(PlaceParameters.PRODUCT_ID, this.state.product.id);
        }
    }

    /*
     * API
     */

    public Promise<Boolean> onAddToCart(Integer quantity) {
        var unexpectedError = app.unexpectedError(caught -> {
            if (caught instanceof DAOOfflineException) {
                this.alertDatabaseNotAvailable();
                LOG.error(this.state.errorMessage, caught);
            } else {
                if (caught instanceof DAOOfflineException) {
                    this.alertDatabaseNotAvailable();
                    LOG.error(this.state.errorMessage, caught);
                } else {
                    app.alertUnexpectedError(LOG, "Adding an item to cart", caught);
                }
            }
        });

        try {
            if (quantity == null) {
                this.errorInvalidQuantity();
                LOG.warn("onAddToCart: " + this.state.errorMessage);
                return Promise.resolve(Boolean.FALSE);
            }

            if (quantity < 1) {
                this.alertCartItemWidthLessThanOneItem();
                LOG.warn("onAddToCart: " + this.state.errorMessage);
                return Promise.resolve(Boolean.FALSE);
            }

            app.getCart().addProduct(this.state.product, quantity);

            final WebFlowIntent place = this.app.newPlace();

            return Routes.cart(this.app, place).catch_(unexpectedError);
        } catch (Exception caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    public Promise<Boolean> onOpenProducts() {
        return Routes.restricted(this.app)

                // OnFailure
                .catch_(caught -> {
                    synchronized (app) {
                        this.app.alertUnexpectedError(LOG, "Going to home of restricted place", caught);
                    }
                    return Promise.resolve(Boolean.FALSE);
                });
    }

    /*
     * Others
     */

    private Promise<ProductItem> loadProductById(Long productId) {
        if (productId == null) {
            return Promise.reject(new WrongParametersException());
        }

        return this.dao.loadProductById(productId)
                // OnSuccess
                .then(product -> {
                    if (product == null) {
                        throw new ProductNotFoundException();
                    }
                    return Promise.resolve(product);
                });
    }

    private void alertDatabaseNotAvailable() {
        this.state.errorCode = 1;
        this.state.errorMessage = "Carrinho não acessível. Aguarde alguns instantes e tente novamente.";
        this.update();
    }

    private void alertCartItemWidthLessThanOneItem() {
        this.state.errorCode = 2;
        this.state.errorMessage = "A quantidade de itens no carrinho deve ser maior ou igual a 1 (um).";
        this.update();
    }

    private void errorInvalidQuantity() {
        this.state.errorCode = 3;
        this.state.errorMessage = "Codificação errada da quantidade.";
        this.update();
    }

}
