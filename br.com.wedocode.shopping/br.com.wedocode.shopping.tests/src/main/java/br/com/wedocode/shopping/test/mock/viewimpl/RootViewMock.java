package br.com.wedocode.shopping.test.mock.viewimpl;

import org.junit.Assert;

import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class RootViewMock extends AbstractViewMock<RootPresenter> {

    public static RootViewMock cast(WebFlowView view) {
        var cls = RootViewMock.class;
        Assert.assertNotNull("Expecting " + cls.getSimpleName() + " but this view was null", view);
        Assert.assertTrue("Expecting " + cls.getSimpleName() + " but it was " + view.getClass().getSimpleName(),
                cls.isInstance(view));
        return (RootViewMock) view;
    }

    public RootPresenter.RootState state;

    public RootViewMock(ShoppingApplicationMock app, RootPresenter presenter) {
        super(app, presenter);
        this.state = presenter.getState();
    }

}
