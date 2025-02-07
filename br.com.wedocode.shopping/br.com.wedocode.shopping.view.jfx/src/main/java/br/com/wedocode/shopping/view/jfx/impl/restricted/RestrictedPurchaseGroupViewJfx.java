package br.com.wedocode.shopping.view.jfx.impl.restricted;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import javafx.scene.layout.VBox;

public class RestrictedPurchaseGroupViewJfx extends AbstractViewJfx<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private int itemIdx;

    private List<RestrictedPurchaseItemViewJfx> viewList;

    private BiConsumer<List<PurchaseInfo>, List<RestrictedPurchaseItemViewJfx>> contentSlot;

    public RestrictedPurchaseGroupViewJfx(ShoppingJfxApplication app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter, new VBox());
        this.state = presenter.getState();
        this.viewList = new ArrayList<>();
    }

    @Override
    public void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        this.contentSlot.accept(this.state.purchases, this.viewList);
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("restricted-purchases-group");

        dom.label(label -> {
            label.getStyleClass().add("caption");
            label.setText("Seu histórico de compras");
        });

        dom.vbox(pane1 -> {
            pane1.getStyleClass().add("content");
            this.contentSlot = newListSlot(pane1, this::newItemView, this::updateItem);
        });
    }

    private RestrictedPurchaseItemViewJfx newItemView() {
        return new RestrictedPurchaseItemViewJfx(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(RestrictedPurchaseItemViewJfx itemView, PurchaseInfo state) {
        itemView.setState(state, false);
        itemView.doUpdate();
    }

}