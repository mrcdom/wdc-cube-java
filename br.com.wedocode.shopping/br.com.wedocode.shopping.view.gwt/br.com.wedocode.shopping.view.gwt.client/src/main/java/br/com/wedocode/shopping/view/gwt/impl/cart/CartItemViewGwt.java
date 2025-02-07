package br.com.wedocode.shopping.view.gwt.impl.cart;

import java.util.Objects;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class CartItemViewGwt extends AbstractViewGwt<CartPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private CartItem state;

    private boolean notRendered = true;

    private HTMLElement nameElm;

    private String nameOldValue;

    private HTMLDivElement priceElm;

    private double priceOldValue;

    private HTMLElement quantityElm;

    private int quantityOldValue;

    public CartItemViewGwt(ShoppingApplicationGwt app, CartPresenter presenter, Integer idx) {
        super(BASE_VIEW_ID + "-" + idx, app, presenter);
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
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.nameOldValue, this.state.name)) {
            this.nameElm.textContent = this.state.name;
            this.nameOldValue = this.state.name;
        }

        if (this.priceOldValue != this.state.price) {
            this.priceElm.textContent = this.formatCurrency(this.state.price);
            this.priceOldValue = this.state.price;
        }

        if (this.quantityOldValue != this.state.quantity) {
            this.quantityElm.textContent = String.valueOf(this.state.quantity);
            this.quantityOldValue = this.state.quantity;
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        dom.div(div0 -> {
            div0.id = "carcel2";

            dom.img(img -> {
                img.className = "mini-img-produto";
                img.src = this.state.image;
            });

            dom.span(span0 -> {
                this.nameElm = span0;
                this.nameElm.textContent = this.state.name;
                this.nameOldValue = this.state.name;
            });
        });

        dom.div(div0 -> {
            div0.id = "carcel2";

            this.priceElm = div0;
            this.priceElm.textContent = this.formatCurrency(this.state.price);
            this.priceOldValue = this.state.price;
        });

        dom.div(div0 -> {
            div0.id = "carcel2b";

            dom.span(span0 -> {
                this.quantityElm = span0;
                this.quantityElm.textContent = String.valueOf(this.state.quantity);
                this.quantityOldValue = this.state.quantity;
            });

            dom.anchor(a0 -> {
                a0.href = "javascript:void(0)";
                dom.img(img -> {
                    img.src = "images/delet.png";
                    img.onclick = this::emitDeleteClicked;
                });
            });
        });
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getDecimalFormat().format(value);
    }

    private Object emitDeleteClicked(Event evt) {
        this.presenter.onRemoveProduct(this.state.id);
        return null;
    }

}