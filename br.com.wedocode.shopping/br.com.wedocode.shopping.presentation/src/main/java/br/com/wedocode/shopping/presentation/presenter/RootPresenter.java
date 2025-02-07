package br.com.wedocode.shopping.presentation.presenter;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.util.StringUtils;
import br.com.wedocode.framework.webflow.WebFlowAbstractPresenter;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.framework.webflow.WebFlowViewSlot;
import br.com.wedocode.shopping.presentation.PlaceAttributes;
import br.com.wedocode.shopping.presentation.PlaceParameters;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.exception.WrongPlace;

public class RootPresenter extends WebFlowAbstractPresenter<ShoppingApplication> {

    private static Logger LOG = LoggerFactory.getLogger(RootPresenter.class);

    public static class RootState {

        public WebFlowView contentView;

        public String errorMessage;

    }

    public static Function<RootPresenter, WebFlowView> createView;

    /*
     * Fields
     */

    private final RootState state = new RootState();

    private final WebFlowViewSlot contentSlot;

    /*
     * Life cycle
     */

    public RootPresenter(ShoppingApplication app) {
        super(app);
        this.contentSlot = this::setContentView;
    }

    @Override
    public void release() {
        this.state.contentView = null;
        super.release();
    }

    public RootState getState() {
        return state;
    }

    /*
     * WebFlow
     */

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        if (initialization) {
            this.view = createView.apply(this);
        }

        if (deepest) {
            throw new WrongPlace();
        } else {
            // Does not accept changing user id at URL
            if (this.app.getSubject() != null) {
                intent.setParameter(PlaceParameters.USER_ID, this.app.getSubject().getId());
            }
            intent.setViewSlot(PlaceAttributes.SLOT_OWNER, this.contentSlot);
        }

        return true;
    }

    @Override
    public void publishParameters(WebFlowIntent intent) {
        if (this.app.getSubject() != null) {
            intent.setParameter(PlaceParameters.USER_ID, this.app.getSubject().getId());
        }
    }

    /*
     * API
     */

    public void alertUnexpectedError(String message, Exception caught) {
        this.alertUnexpectedError(LOG, message, caught);
    }

    public void alertUnexpectedError(Logger logger, String message, Throwable caught) {
        if (StringUtils.isNotBlank(caught.getMessage())) {
            this.state.errorMessage = message;
        } else {
            this.state.errorMessage = message + ": " + caught.getMessage();
        }
        this.update();
        LOG.error(this.state.errorMessage, caught);
    }

    /*
     * Internal
     */

    private void setContentView(WebFlowView view) {
        if (this.state.contentView != view) {
            this.state.contentView = view;
            this.update();
        }
    }

}
