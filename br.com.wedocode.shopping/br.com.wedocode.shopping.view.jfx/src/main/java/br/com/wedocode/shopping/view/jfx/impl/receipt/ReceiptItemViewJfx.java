package br.com.wedocode.shopping.view.jfx.impl.receipt;

import java.text.NumberFormat;
import java.util.Objects;

import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptItem;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ReceiptItemViewJfx extends AbstractViewJfx<ReceiptPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private ReceiptItem state;

    private boolean notRendered = true;

    private Label quantityElm;

    private int quantityOldValue;

    private Label descriptionElm;

    private String descriptionOldValue;

    private Label priceElm;

    private double priceOldValue;

    public ReceiptItemViewJfx(ShoppingJfxApplication app, ReceiptPresenter presenter, Integer idx) {
        super(BASE_VIEW_ID + "-" + idx, app, presenter, new HBox());
    }

    public void setState(ReceiptItem state, boolean scheduleUpdate) {
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

        if (!Objects.equals(this.descriptionOldValue, this.state.description)) {
            this.descriptionElm.setText(this.state.description);
            this.descriptionOldValue = this.state.description;
        }

        if (this.priceOldValue != this.state.value) {
            this.priceElm.setText(formatCurrency(this.state.value));
            this.priceOldValue = this.state.value;
        }

        if (this.quantityOldValue != this.state.quantity) {
            this.quantityElm.setText(String.valueOf(this.state.quantity));
            this.quantityOldValue = this.state.quantity;
        }
    }

    private void initialRender(JfxDom dom, HBox pane0) {
        pane0.getStyleClass().add("row");

        dom.label(label -> {
            label.getStyleClass().add("cell-1");
            this.descriptionElm = label;
            this.descriptionElm.setText(this.state.description);
            this.descriptionOldValue = this.state.description;
        });

        dom.label(label -> {
            label.getStyleClass().add("cell-2");
            this.priceElm = label;
            this.priceElm.setText(formatCurrency(this.state.value));
            this.priceOldValue = this.state.value;
        });

        dom.label(label -> {
            label.getStyleClass().add("cell-3");
            this.quantityElm = label;
            this.quantityElm.setText(String.valueOf(this.state.quantity));
            this.quantityOldValue = this.state.quantity;
        });
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

}
