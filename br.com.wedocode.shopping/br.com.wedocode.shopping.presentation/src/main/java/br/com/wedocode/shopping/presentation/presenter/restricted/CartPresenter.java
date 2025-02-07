package br.com.wedocode.shopping.presentation.presenter.restricted;

import java.util.Collections;
import java.util.List;
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
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOOfflineException;
import br.com.wedocode.shopping.presentation.shared.business.exception.InvalidCartItemDAOException;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;

public class CartPresenter extends WebFlowAbstractPresenter<ShoppingApplication> {

    private static final Logger LOG = LoggerFactory.getLogger(CartPresenter.class.getName());

    public static class CartState {

        public List<CartItem> items;

        public int errorCode;

        public String errorMessage;
    }

    public static Function<CartPresenter, WebFlowView> createView;

    /*
     * Fields
     */

    private final CartState state = new CartState();

    private CartManager cart;

    private WebFlowViewSlot ownerSlot;

    /*
     * Life cycle
     */

    public CartPresenter(ShoppingApplication app) {
        super(app);
        this.cart = app.getCart();
        this.state.items = Collections.emptyList();
    }

    @Override
    public void release() {
        super.release();
    }

    public CartState getState() {
        return state;
    }

    /*
     * WebFlow
     */

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        this.state.items = this.cart.getCartItems();

        if (initialization) {
            this.ownerSlot = intent.getViewSlot(PlaceAttributes.SLOT_OWNER);
            this.view = createView.apply(this);
        }

        this.ownerSlot.setView(this.view);

        return true;
    }

    /*
     * API
     */

    public Promise<Boolean> onModifyQuantity(Long productId, Integer quantity) {
        return new Promise<Boolean>((resolve, reject) -> {
            try {
                if (productId == null) {
                    this.errorCodigoDeProdutoMalFormatado();
                    LOG.warn("onModifyQuantity: " + this.state.errorMessage);
                    resolve.accept(Boolean.FALSE);
                    return;
                }

                if (quantity == null) {
                    this.errorValorQuantidadeMalFormatado();
                    LOG.warn("onModifyQuantity: " + this.state.errorMessage);
                    resolve.accept(Boolean.FALSE);
                    return;
                }

                if (quantity < 1) {
                    this.alertThereIsItemWhichValueIsLessThanOne();
                    LOG.warn("onModifyQuantity: " + this.state.errorMessage);
                    resolve.accept(Boolean.FALSE);
                    return;
                }

                var found = this.cart.modifyProductQuantity(productId, quantity);
                if (!found) {
                    this.alertProductNotFound();
                    LOG.warn("onModifyQuantity: " + this.state.errorMessage);
                    resolve.accept(Boolean.FALSE);
                    return;
                }
                resolve.accept(Boolean.TRUE);
            } catch (Throwable caught) {
                this.app.alertUnexpectedError(LOG, "Removing a prouduct", caught);
                resolve.accept(Boolean.FALSE);
            }
        });
    }

    public Promise<Boolean> onRemoveProduct(Long productId) {
        var unexpectedError = app.unexpectedError(caught -> {
            app.alertUnexpectedError(LOG, "Removing a prouduct", caught);
        });

        try {
            if (productId == null) {
                this.errorCodigoDeProdutoMalFormatado();
                LOG.warn("onRemoveProduct: " + this.state.errorMessage);
                return Promise.resolve(Boolean.FALSE);
            }

            var modified = this.cart.removeProduct(productId);
            if (modified) {
                if (this.cart.getSize() == 0) {
                    return Routes.restricted(this.app)

                            // OnFailure
                            .catch_(unexpectedError);
                } else {
                    this.state.items = this.cart.getCartItems();
                    this.update();
                }
            }
            return Promise.resolve(Boolean.TRUE);
        } catch (Throwable caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    public Promise<Boolean> onBuy() {
        var unexpectedError = app.unexpectedError(caught -> {
            if (caught instanceof InvalidCartItemDAOException) {
                this.alertThereIsItemWhichValueIsLessThanOne();
                LOG.error("onBuy: " + this.state.errorMessage, caught);
            } else if (caught instanceof DAOOfflineException) {
                this.alertDatabaseOffline();
                LOG.error("onBuy: " + this.state.errorMessage, caught);
            } else {
                this.app.alertUnexpectedError(LOG, "Buying an product", caught);
            }
        });

        try {

            if (this.cart.getSize() == 0) {
                this.alertPurchaseOfEmptyCart();
                LOG.warn("onBuy: " + this.state.errorMessage);
                return Promise.resolve(Boolean.FALSE);
            }

            return this.cart.commit(this.app.getSubject())

                    // onSuccess
                    .then(purchaseId -> {
                        synchronized (app) {
                            final var place = this.app.newPlace();
                            place.setParameter(PlaceParameters.PURCHASE_ID, purchaseId);
                            place.setAttribute(PlaceAttributes.ATTR_PURCHASE_MADE, Boolean.TRUE);
                            return Routes.receipt(this.app, place);
                        }
                    })

                    // onFailure
                    .catch_(unexpectedError);
        } catch (Throwable caught) {
            unexpectedError.apply(caught);
            return Promise.resolve(Boolean.FALSE);
        }

    }

    public Promise<Boolean> onOpenProducts() {
        return Routes.restricted(this.app)

                // OnFailure
                .catch_(caught -> {
                    synchronized (app) {
                        this.app.alertUnexpectedError(LOG, "Going to root restricted place", caught);
                    }
                    return Promise.resolve(Boolean.FALSE);
                });
    }

    /*
     * Others
     */

    public void alertThereIsItemWhichValueIsLessThanOne() {
        this.state.errorCode = 1;
        this.state.errorMessage = "Deve existir pelo menos um item no carrinhro para se efetivar uma compra";
        this.update();
    }

    public void alertProductNotFound() {
        this.state.errorCode = 2;
        this.state.errorMessage = "Produdo não localizado na base dados.";
        this.update();
    }

    public void alertPurchaseOfEmptyCart() {
        this.state.errorCode = 3;
        this.state.errorMessage = "Existem produtos com menos de um item na quantidade. Impossível comprar.";
        this.update();
    }

    public void alertDatabaseOffline() {
        this.state.errorCode = 4;
        this.state.errorMessage = "O banco de dados encontra-se fora do ar no momento. Aguarde alguns instantes e tente novamente.";
        this.update();
    }

    public void errorCodigoDeProdutoMalFormatado() {
        this.state.errorCode = 5;
        this.state.errorMessage = "Código do produto mal formado.";
        this.update();
    }

    public void errorValorQuantidadeMalFormatado() {
        this.state.errorCode = 6;
        this.state.errorMessage = "Valor da quantiade está mal formado.";
        this.update();
    }

}
