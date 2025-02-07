package br.com.wedocode.shopping.view.jfx.impl.cart;

import java.text.NumberFormat;
import java.util.Objects;

import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import br.com.wedocode.shopping.view.jfx.util.ResourceCatalog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class CartItemViewJfx extends AbstractViewJfx<CartPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private CartItem state;

    private boolean notRendered = true;

    private Label nameElm;

    private String nameOldValue;

    private Label priceElm;

    private double priceOldValue;

    private Label quantityElm;

    private int quantityOldValue;

    public CartItemViewJfx(ShoppingJfxApplication app, CartPresenter presenter, Integer idx) {
        super(BASE_VIEW_ID + "-" + idx, app, presenter, new HBox());
    }

    public void setState(CartItem state, boolean scheduleUpdate) {
        this.state = state;
        if (scheduleUpdate) {
            this.update();
        }
    }

    @Override
    public void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((HBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.nameOldValue, this.state.name)) {
            this.nameElm.setText(this.state.name);
            this.nameOldValue = this.state.name;
        }

        if (this.priceOldValue != this.state.price) {
            this.priceElm.setText(this.formatCurrency(this.state.price));
            this.priceOldValue = this.state.price;
        }

        if (this.quantityOldValue != this.state.quantity) {
            this.quantityElm.setText(String.valueOf(this.state.quantity));
            this.quantityOldValue = this.state.quantity;
        }
    }

    private void initialRender(JfxDom dom, HBox pane0) {
        pane0.getStyleClass().add("row");

        // Column 0
        dom.hbox(pane1 -> {
            pane1.getStyleClass().add("cell-1");
            // pane1.setAlignment(Pos.CENTER_LEFT);

            dom.img(img -> {
                img.setFitWidth(42);
                img.setFitHeight(40);
                img.setImage(ResourceCatalog.getImage(this.state.image));
            });

            dom.label(label -> {
                this.nameElm = label;
                this.nameElm.setText(this.state.name);
                this.nameOldValue = this.state.name;
            });
        });

        // Column 1
        dom.label(pane1 -> {
            pane1.getStyleClass().add("cell-1");

            this.priceElm = pane1;
            this.priceElm.setText(this.formatCurrency(this.state.price));
            this.priceOldValue = this.state.price;
        });

        // Column 2
        dom.hbox(pane1 -> {
            pane1.getStyleClass().add("cell-3");

            dom.label(label -> {
                this.quantityElm = label;
                this.quantityElm.setText(String.valueOf(this.state.quantity));
                this.quantityOldValue = this.state.quantity;
            });

            dom.img(img -> {
                img.setImage(ResourceCatalog.getImage("images/delet.png"));
                img.setOnMouseClicked(this::emitDeleteClicked);
            });
        });
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getInstance().format(value);
    }

    private void emitDeleteClicked(MouseEvent evt) {
        this.presenter.onRemoveProduct(this.state.id);
    }

}