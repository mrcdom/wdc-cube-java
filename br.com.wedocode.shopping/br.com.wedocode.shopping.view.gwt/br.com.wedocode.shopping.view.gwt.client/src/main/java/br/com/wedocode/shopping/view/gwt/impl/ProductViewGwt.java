package br.com.wedocode.shopping.view.gwt.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.CSSProperties.PaddingTopUnionType;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;

public class ProductViewGwt extends AbstractViewGwt<ProductPresenter> {

    private static Logger LOG = LoggerFactory.getLogger(ProductViewGwt.class);

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    public ProductPresenter.ProductState state;

    private boolean notRendered = true;

    private HTMLElement nameElm1;

    private HTMLElement nameElm2;

    private String nameOldValue;

    private HTMLImageElement imageElm;

    private String imageOldValue;

    private HTMLElement priceElm;

    private double priceOldValue;

    private HTMLInputElement quantityElm;

    private HTMLDivElement descriptionElm;

    private String descriptionOldValue;

    private HTMLDivElement errorElm;

    public ProductViewGwt(ShoppingApplicationGwt app, ProductPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.nameOldValue, this.state.product.name)) {
            this.nameElm1.textContent = this.state.product.name;
            this.nameElm2.textContent = this.state.product.name;
            this.nameOldValue = this.state.product.name;
        }

        if (!Objects.equals(this.imageOldValue, this.state.product.image)) {
            this.imageElm.src = this.state.product.image;
            this.imageOldValue = this.state.product.image;
        }

        if (this.priceOldValue != this.state.product.price) {
            this.priceElm.textContent = formatCurrency(this.state.product.price);
            this.priceOldValue = this.state.product.price;
        }

        if (!Objects.equals(this.descriptionOldValue, this.state.product.description)) {
            this.descriptionElm.innerHTML = this.state.product.description;
            this.descriptionOldValue = this.state.product.description;
        }

        var newErrorDisplay = "none";
        var newErrorMessage = "";
        if (this.state.errorCode != 0) {
            newErrorDisplay = "block";
            newErrorMessage = this.state.errorMessage;

            this.state.errorCode = 0;
            this.state.errorMessage = null;
        }

        if (!Objects.equals(this.errorElm.textContent, newErrorMessage)) {
            this.errorElm.textContent = newErrorMessage;
        }

        if (!Objects.equals(this.errorElm.style.display, newErrorDisplay)) {
            this.errorElm.style.display = newErrorDisplay;
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.id = "boxProdutos";

        dom.div(div0 -> {
            div0.id = "breadcrumb";

            dom.paragraph(p0 -> {
                dom.bold(b0 -> {
                    dom.span(span0 -> span0.innerHTML = "Produtos &gt;&nbsp;");
                    dom.span(span0 -> {
                        this.nameElm1 = span0;
                        this.nameElm1.textContent = this.state.product.name;
                        this.nameOldValue = this.state.product.name;
                    });
                });
            });
        });

        dom.div(div0 -> {
            div0.id = "fotoproduto";

            dom.img(img -> {
                img.id = "logo";
                this.imageElm = img;
                this.imageElm.src = this.state.product.image;
                this.imageOldValue = this.state.product.image;
            });
        });

        dom.div(div0 -> {
            div0.id = "gdescricao";

            dom.div(div1 -> {
                div1.id = "tituloProduto";

                dom.header(1, h0 -> {
                    this.nameElm2 = h0;
                    this.nameElm2.textContent = this.state.product.name;
                });

                dom.header(2, h0 -> {
                    this.priceElm = h0;
                    this.priceElm.textContent = formatCurrency(this.state.product.price);
                    this.priceOldValue = this.state.product.price;
                });

                dom.div(div2 -> {
                    div2.id = "quantidade";

                    dom.span(span0 -> span0.textContent = "Quantidade:");

                    dom.input(input0 -> {
                        input0.type = "text";
                        input0.name = "quantidade";

                        this.quantityElm = input0;
                        this.quantityElm.value = "1";
                    });
                });

                dom.div(div2 -> {
                    div2.id = "descricao";
                    div2.style.paddingTop = PaddingTopUnionType.of("30px");

                    dom.header(1, h0 -> h0.textContent = "DESCRIÇÃO DO PRODUTO");

                    dom.div(div3 -> {
                        this.descriptionElm = div3;
                        this.descriptionElm.innerHTML = this.state.product.description;
                        this.descriptionOldValue = this.state.product.description;
                    });

                    dom.div(div3 -> {
                        div3.id = "comprar";

                        dom.button(button0 -> {
                            button0.onclick = this::emitBuyClicked;

                            dom.anchor(a0 -> {
                                a0.className = "link";
                                a0.href = "javascript:void(0)";
                                a0.textContent = "COMPRAR";
                            });
                        });
                    });
                });

                dom.div(div2 -> {
                    this.errorElm = div2;
                    this.errorElm.id = "error";
                    this.errorElm.style.display = this.state.errorCode != 0 ? "block" : "none";
                    this.errorElm.textContent = this.state.errorMessage;
                });
            });

        });

        dom.div(div0 -> {
            div0.id = "naveg";

            dom.button(button0 -> {
                button0.onclick = this::emitBackClicked;

                dom.anchor(a0 -> {
                    a0.className = "link";
                    a0.href = "javascript:void(0)";
                    a0.textContent = "< VOLTAR";
                });
            });
        });
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getDecimalFormat().format(value);
    }

    private Object emitBackClicked(Event evt) {
        this.presenter.onOpenProducts();
        return null;
    }

    private Object emitBuyClicked(Event evt) {
        var quantity = 1;
        try {
            quantity = Integer.parseInt(this.quantityElm.value);
        } catch (NumberFormatException caught) {
            LOG.error("Trying to parse value: " + this.quantityElm.value, caught);
        }
        this.presenter.onAddToCart(quantity);
        return null;
    }

}
