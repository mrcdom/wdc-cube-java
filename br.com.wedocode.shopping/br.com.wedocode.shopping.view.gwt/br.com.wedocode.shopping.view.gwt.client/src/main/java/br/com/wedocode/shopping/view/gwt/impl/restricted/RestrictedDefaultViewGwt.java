package br.com.wedocode.shopping.view.gwt.impl.restricted;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.HTMLDivElement;

public class RestrictedDefaultViewGwt extends AbstractViewGwt<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private RestrictedPurchaseGroupViewGwt purchasesView;

    private RestrictedProductGroupViewGwt productsView;

    public RestrictedDefaultViewGwt(ShoppingApplicationGwt app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
        this.purchasesView = new RestrictedPurchaseGroupViewGwt(app, this.presenter);
        this.productsView = new RestrictedProductGroupViewGwt(app, this.presenter);
    }

    @Override
    public void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        if (this.state.purchases != null && this.state.purchases.size() > 0) {
            // It is needed because this view is not known by presenter
            this.purchasesView.doUpdate();

            if (this.purchasesView.getElement().parentElement != this.element) {
                this.element.insertBefore(this.purchasesView.getElement(), this.element.firstChild);
            }
        }

        // If no purchases have been made, remove menu
        else if (this.purchasesView.getElement().parentElement != null) {
            this.purchasesView.getElement().parentElement.removeChild(this.purchasesView.getElement());
        }

        // It is needed because this view is not known by presenter
        this.productsView.doUpdate();
        if (this.productsView.getElement().parentElement != this.element) {
            this.element.appendChild(this.productsView.getElement());
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.style.display = "flex";
        viewDiv.style.flexDirection = "row";
        viewDiv.style.set("gap", "10px");
    }

}