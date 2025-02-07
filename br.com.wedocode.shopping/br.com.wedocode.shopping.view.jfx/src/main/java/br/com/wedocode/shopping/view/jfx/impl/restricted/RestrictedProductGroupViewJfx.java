package br.com.wedocode.shopping.view.jfx.impl.restricted;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class RestrictedProductGroupViewJfx extends AbstractViewJfx<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private int itemIdx;

    private List<RestrictedProductItemViewJfx> itemViewList;

    private BiConsumer<List<ProductItem>, List<RestrictedProductItemViewJfx>> contentSlot;

    public RestrictedProductGroupViewJfx(ShoppingJfxApplication app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter, new VBox());
        this.state = presenter.getState();
        this.itemViewList = new ArrayList<>();
    }

    @Override
    public void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        this.contentSlot.accept(this.state.products, this.itemViewList);
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("restricted-product-group");

        dom.label(label -> {
            label.getStyleClass().add("caption");
            label.setText("PRODUTOS");
            label.setTextAlignment(TextAlignment.CENTER);
        });

        dom.textFlow(pane1 -> {
            this.contentSlot = newListSlot(pane1, this::newItemView, this::updateItem);
        });
    }

    private RestrictedProductItemViewJfx newItemView() {
        return new RestrictedProductItemViewJfx(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(RestrictedProductItemViewJfx itemView, ProductItem state) {
        itemView.setState(state, false);
        itemView.doUpdate();
    }

}