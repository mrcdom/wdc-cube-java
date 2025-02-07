package br.com.wedocode.shopping.view.gwt.impl.restricted;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.HTMLDivElement;

public class RestrictedPurchaseGroupViewGwt extends AbstractViewGwt<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private int itemIdx;

    private List<RestrictedPurchaseItemViewGwt> viewList;

    private BiConsumer<List<PurchaseInfo>, List<RestrictedPurchaseItemViewGwt>> contentSlot;

    public RestrictedPurchaseGroupViewGwt(ShoppingApplicationGwt app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
        this.viewList = new ArrayList<>();
    }

    public void setState(RestrictedPresenter.RestrictedState state) {
        this.setState(state, true);
    }

    public void setState(RestrictedPresenter.RestrictedState state, boolean scheduleUpdate) {
        this.state = state;
        if (scheduleUpdate) {
            this.update();
        }
    }

    @Override
    public void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        this.contentSlot.accept(this.state.purchases, this.viewList);
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.id = "menu";

        dom.header(4, h0 -> {
            h0.textContent = "Seu histórico de compras";
        });

        dom.div(div1 -> {
            this.contentSlot = newListSlot(div1, this::newItemView, this::updateItem);
        });
    }

    private RestrictedPurchaseItemViewGwt newItemView() {
        return new RestrictedPurchaseItemViewGwt(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(RestrictedPurchaseItemViewGwt itemView, PurchaseInfo state) {
        itemView.setState(state, false);
        itemView.doUpdate();
    }

}