package br.com.wedocode.shopping.view.gwt.impl.restricted;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.HTMLDivElement;

public class RestrictedProductGroupViewGwt extends AbstractViewGwt<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private int itemIdx;

    private List<RestrictedProductItemViewGwt> itemViewList;

    private BiConsumer<List<ProductItem>, List<RestrictedProductItemViewGwt>> contentSlot;

    public RestrictedProductGroupViewGwt(ShoppingApplicationGwt app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
        this.itemViewList = new ArrayList<>();
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

        this.contentSlot.accept(this.state.products, this.itemViewList);
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.id = "boxProdutos";

        viewDiv.style.display = "flex";
        viewDiv.style.flexDirection = "column";

        dom.header(1, h0 -> {
            h0.textContent = "PRODUTOS";
        });

        dom.div(div0 -> {
            this.contentSlot = newListSlot(div0, this::newItemView, this::updateItem);
        });
    }

    private RestrictedProductItemViewGwt newItemView() {
        return new RestrictedProductItemViewGwt(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(RestrictedProductItemViewGwt itemView, ProductItem state) {
        itemView.setState(state, false);
        itemView.doUpdate();
    }

}