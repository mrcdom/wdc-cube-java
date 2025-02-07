package br.com.wedocode.shopping.presentation.presenter.nonrestricted;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowAbstractPresenter;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.framework.webflow.WebFlowViewSlot;
import br.com.wedocode.shopping.presentation.PlaceAttributes;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOOfflineException;

public class LoginPresenter extends WebFlowAbstractPresenter<ShoppingApplication> {

    private static Logger LOG = LoggerFactory.getLogger(LoginPresenter.class);

    public class LoginState {

        public String userName;

        public String password;

        public int errorCode;

        public String errorMessage;

    }

    public static Function<LoginPresenter, WebFlowView> createView;

    /*
     * Fields
     */

    private final LoginState state = new LoginState();

    private ShoppingDAO dao;

    private WebFlowViewSlot ownerSlot;

    /*
     * Life cycle
     */

    public LoginPresenter(ShoppingApplication app) {
        super(app);
        this.dao = ShoppingContext.getDAO();
    }

    public LoginState getState() {
        return state;
    }

    @Override
    public void release() {
        super.release();
    }

    /*
     * Webflow
     */

    @Override
    public boolean applyParameters(WebFlowIntent intent, boolean initialization, boolean deepest) throws Exception {
        if (initialization) {
            ownerSlot = intent.getViewSlot(PlaceAttributes.SLOT_OWNER);
            this.view = createView.apply(this);
        }

        ownerSlot.setView(view);

        return false;
    }

    /*
     * Messages
     */

    private void alertUserOrPasswordNotRecognize() {
        state.errorCode = 1;
        state.errorMessage = "Usuário ou senha não reconhecido!";
        this.update();
    }

    private void alertDatabaseIsOffline() {
        state.errorCode = 4;
        state.errorMessage = "Banco de dados esta fora do ar!";
        this.update();
    }

    /*
     * API
     */

    public Promise<Boolean> onEnter() {
        return this.dao.loadSubject(state.userName, state.password)

                // OnSuccess
                .then(subject -> {
                    synchronized (app) {
                        if (subject == null || subject.getId() == null) {
                            app.setSubject(null);

                            this.alertUserOrPasswordNotRecognize();
                            return Promise.resolve(Boolean.FALSE);
                        } else {
                            app.setSubject(subject);

                            return Routes.restricted(app);
                        }
                    }
                })

                // OnFailure
                .catch_(caught -> {
                    synchronized (app) {
                        if (caught instanceof DAOOfflineException) {
                            this.alertDatabaseIsOffline();
                        } else {
                            LOG.error("onEnter", caught);
                            app.alertUnexpectedError(LOG, "Trying to access restricted area", caught);
                        }
                    }
                    return Promise.resolve(Boolean.FALSE);
                });
    }

}
