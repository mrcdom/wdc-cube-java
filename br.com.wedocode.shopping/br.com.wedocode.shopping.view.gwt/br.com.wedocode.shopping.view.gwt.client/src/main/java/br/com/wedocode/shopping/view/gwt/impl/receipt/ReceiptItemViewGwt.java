package br.com.wedocode.shopping.view.gwt.impl.receipt;

import java.util.Objects;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptItem;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class ReceiptItemViewGwt extends AbstractViewGwt<ReceiptPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private ReceiptItem state;

    private boolean notRendered = true;

    private HTMLElement quantityElm;

    private int quantityOldValue;

    private HTMLDivElement descriptionElm;

    private String descriptionOldValue;

    private HTMLDivElement priceElm;

    private double priceOldValue;

    public ReceiptItemViewGwt(ShoppingApplicationGwt app, ReceiptPresenter presenter, Integer idx) {
        super(BASE_VIEW_ID + "-" + idx, app, presenter);
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
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.descriptionOldValue, this.state.description)) {
            this.descriptionElm.textContent = this.state.description;
            this.descriptionOldValue = this.state.description;
        }

        if (this.priceOldValue != this.state.value) {
            this.priceElm.textContent = formatCurrency(this.state.value);
            this.priceOldValue = this.state.value;
        }

        if (this.quantityOldValue != this.state.quantity) {
            this.quantityElm.textContent = String.valueOf(this.state.quantity);
            this.quantityOldValue = this.state.quantity;
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        dom.div(div0 -> div0.id = "borda");

        dom.div(div0 -> {
            div0.id = "cel1";

            this.descriptionElm = div0;
            this.descriptionElm.textContent = this.state.description;
            this.descriptionOldValue = this.state.description;
        });

        dom.div(div0 -> {
            div0.id = "cel1";

            this.priceElm = div0;
            this.priceElm.textContent = formatCurrency(this.state.value);
            this.priceOldValue = this.state.value;
        });

        dom.div(div0 -> {
            div0.id = "cel1";

            this.quantityElm = div0;
            this.quantityElm.textContent = String.valueOf(this.state.quantity);
            this.quantityOldValue = this.state.quantity;
        });
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getDecimalFormat().format(value);
    }

}
