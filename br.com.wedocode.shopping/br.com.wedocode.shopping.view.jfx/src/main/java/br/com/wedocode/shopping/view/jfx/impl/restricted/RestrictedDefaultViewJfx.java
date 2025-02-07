package br.com.wedocode.shopping.view.jfx.impl.restricted;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxUtil;
import javafx.scene.layout.HBox;

public class RestrictedDefaultViewJfx extends AbstractViewJfx<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private RestrictedPurchaseGroupViewJfx purchasesView;

    private RestrictedProductGroupViewJfx productsView;

    public RestrictedDefaultViewJfx(ShoppingJfxApplication app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter, new HBox());
        this.state = presenter.getState();
        this.purchasesView = new RestrictedPurchaseGroupViewJfx(app, this.presenter);
        this.productsView = new RestrictedProductGroupViewJfx(app, this.presenter);
    }

    @Override
    public void doUpdate() {
        var pane0 = (HBox) this.element;

        if (this.notRendered) {
            pane0.setSpacing(10);
            this.notRendered = false;
        }

        if (this.state.purchases != null && this.state.purchases.size() > 0) {
            // It is needed because this view is not known by presenter
            this.purchasesView.doUpdate();

            if (this.purchasesView.getElement().getParent() != pane0) {
                pane0.getChildren().add(0, this.purchasesView.getElement());
            }
        }

        // If no purchases have been made, remove menu
        else {
            JfxUtil.removeFromParent(this.purchasesView.getElement());
        }

        // It is needed because this view is not known by presenter
        this.productsView.doUpdate();

        if (this.productsView.getElement().getParent() != this.element) {
            JfxUtil.removeFromParent(this.productsView.getElement());
            pane0.getChildren().add(this.productsView.getElement());
        }
    }

}