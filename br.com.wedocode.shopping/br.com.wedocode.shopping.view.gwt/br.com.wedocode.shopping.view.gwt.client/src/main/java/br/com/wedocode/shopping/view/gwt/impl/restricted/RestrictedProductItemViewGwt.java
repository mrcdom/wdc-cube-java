package br.com.wedocode.shopping.view.gwt.impl.restricted;

import java.util.Objects;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.CSSProperties.HeightUnionType;
import elemental2.dom.CSSProperties.WidthUnionType;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;

public class RestrictedProductItemViewGwt extends AbstractViewGwt<RestrictedPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private ProductItem state;

    private boolean notRendered = true;

    private HTMLImageElement imageElm;

    private String imageOldValue;

    private HTMLElement nameElm;

    private String nameOldValue;

    private HTMLElement priceElm;

    private double priceOldValue;

    public RestrictedProductItemViewGwt(ShoppingApplicationGwt app, WebFlowPresenter presenter, int idx) {
        super(BASE_VIEW_ID + "-" + idx, app, (RestrictedPresenter) presenter);
    }

    public void setState(ProductItem state) {
        this.setState(state, true);
    }

    public void setState(ProductItem state, boolean scheduleUpdate) {
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

        if (!Objects.equals(this.imageOldValue, this.state.image)) {
            this.imageElm.src = this.state.image;
            this.imageOldValue = this.state.image;
        }

        if (!Objects.equals(this.nameOldValue, this.state.name)) {
            this.nameElm.textContent = this.state.name;
            this.nameOldValue = this.state.name;
        }

        if (this.priceOldValue != this.state.price) {
            this.priceElm.textContent = getPriceStr();
            this.priceOldValue = this.state.price;
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.id = "minibox";
        viewDiv.className = "product-box";
        viewDiv.onclick = this::emitClicked;

        dom.div(div1 -> {
            div1.id = "afoto";

            dom.img(img -> {
                img.style.width = WidthUnionType.of("194px");
                img.style.height = HeightUnionType.of("152px");

                this.imageElm = img;
                this.imageOldValue = this.state.image;
                this.imageElm.src = this.state.image;
            });
        });

        dom.div(div1 -> {
            div1.id = "bfoto";

            dom.anchor(a0 -> {
                a0.className = "link";
                a0.href = "javascript:void(0)";

                dom.span(span0 -> {
                    this.nameElm = span0;
                    this.nameElm.textContent = this.state.name;
                    this.nameOldValue = this.state.name;
                });

                dom.br();

                dom.bold(b0 -> {
                    this.priceElm = b0;
                    this.priceElm.textContent = getPriceStr();
                    this.priceOldValue = this.state.price;
                });
            });
        });
    }

    private String getPriceStr() {
        return "R$ " + NumberFormat.getDecimalFormat().format(this.state.price);
    }

    private Object emitClicked(Event evt) {
        this.presenter.onOpenProduct(this.state.id);
        return null;
    }

}