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
import br.com.wedocode.shopping.presentation.exception.PurchaseNotFoundException;
import br.com.wedocode.shopping.presentation.exception.WrongParametersException;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;

public class ReceiptPresenter extends WebFlowAbstractPresenter<ShoppingApplication> {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiptPresenter.class.getName());

    public static class ReceiptState {

        public boolean notifySuccess;

        public ReceiptForm receipt;

    }

    public static Function<ReceiptPresenter, WebFlowView> createView;

    /*
     * Fields
     */

    private final ReceiptState state = new ReceiptState();

    private ShoppingDAO dao;

    private Long purchaseId;

    private WebFlowViewSlot ownerSlot;

    /*
     * Life cycle
     */

    public ReceiptPresenter(ShoppingApplication app) {
        super(app);
        this.dao = ShoppingContext.getDAO();
    }

    @Override
    public void release() {
        super.release();
    }

    public ReceiptState getState() {
        return state;
    }

    /*
     * WebFlow
     */

    @Override
    public Promise<Boolean> resolveParameters(WebFlowIntent place) {
        var purchaseId = place.getParameterAsLong(PlaceParameters.PURCHASE_ID, this.purchaseId);
        if (purchaseId == null) {
            throw new AssertionError("Missing PURCHASE_ID");
        }

        if (this.state.receipt == null || !Objects.equals(purchaseId, this.purchaseId)) {
            return this.loadReceipt(purchaseId)

                    // OnSuccess
                    .then(receipt -> {
                        synchronized (app) {
                            this.purchaseId = purchaseId;
                            this.state.receipt = receipt;
                            update();
                        }
                        return Promise.resolve(Boolean.TRUE);
                    });
        }

        return Promise.resolve(Boolean.TRUE);
    }

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        this.state.notifySuccess = Boolean.TRUE.equals(intent.getAttribute(PlaceAttributes.ATTR_PURCHASE_MADE));

        if (initialization) {
            this.ownerSlot = intent.getViewSlot(PlaceAttributes.SLOT_OWNER);

            if (this.state.receipt == null) {
                throw new AssertionError("Missing receipt");
            }

            this.view = createView.apply(this);
        }

        this.ownerSlot.setView(this.view);

        return true;
    }

    @Override
    public void publishParameters(WebFlowIntent intent) {
        if (this.purchaseId != null) {
            intent.setParameter(PlaceParameters.PURCHASE_ID, this.purchaseId);
        }
    }

    /*
     * API
     */

    public Promise<Boolean> onPrint() {
        try {
            // TODO
            return Promise.resolve(Boolean.TRUE);
        } catch (Exception caught) {
            this.app.alertUnexpectedError(LOG, "Trying to print receipt", caught);
            return Promise.resolve(Boolean.FALSE);
        }
    }

    public Promise<Boolean> onOpenProducts() {
        return Routes.restricted(this.app)

                // OnFailure
                .catch_(caught -> {
                    synchronized (app) {
                        this.app.alertUnexpectedError(LOG, "Going to restricted home place", caught);
                    }
                    return Promise.resolve(Boolean.FALSE);
                });
    }

    /*
     * Others
     */

    private Promise<ReceiptForm> loadReceipt(Long purchaseId) {
        if (purchaseId == null) {
            return Promise.reject(new WrongParametersException());
        }

        return this.dao.loadReceipt(purchaseId)

                // OnSuccess
                .then(receipt -> {
                    if (receipt == null) {
                        throw new PurchaseNotFoundException();
                    }

                    return Promise.resolve(receipt);
                });
    }

}
