package br.com.wedocode.shopping.test.mock.viewimpl;

import org.junit.Assert;

import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class LoginViewMock extends AbstractViewMock<LoginPresenter> {

    public static LoginViewMock cast(WebFlowView view) {
        var cls = LoginViewMock.class;
        Assert.assertNotNull("Expecting " + cls.getSimpleName() + " but this view was null", view);
        Assert.assertTrue("Expecting " + cls.getSimpleName() + " but it was " + view.getClass().getSimpleName(),
                cls.isInstance(view));
        return (LoginViewMock) view;
    }

    public LoginPresenter.LoginState state;

    public LoginViewMock(ShoppingApplicationMock app, LoginPresenter presenter) {
        super(app, presenter);
        this.state = presenter.getState();
    }

}
