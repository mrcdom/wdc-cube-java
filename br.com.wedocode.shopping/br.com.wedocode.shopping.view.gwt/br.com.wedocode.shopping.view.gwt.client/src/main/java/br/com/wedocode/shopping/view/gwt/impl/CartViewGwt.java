package br.com.wedocode.shopping.view.gwt.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.cart.CartItemViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class CartViewGwt extends AbstractViewGwt<CartPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    protected CartPresenter.CartState state;

    private boolean notRendered = true;

    private List<CartItemViewGwt> cartItemViewList;

    private BiConsumer<List<CartItem>, List<CartItemViewGwt>> itemsSlot;

    private int itemIdx;

    private HTMLElement itemSizeElm;

    private int itemSizeOldValue;

    private HTMLElement totalCostElm;

    private double totalCostOldValue;

    private HTMLDivElement errorElm;

    public CartViewGwt(ShoppingApplicationGwt app, CartPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
        this.cartItemViewList = new ArrayList<>();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        if (this.itemSizeOldValue != this.state.items.size()) {
            this.itemSizeElm.textContent = "[" + this.state.items.size() + "]";
            this.itemSizeOldValue = this.state.items.size();
        }

        this.itemsSlot.accept(this.state.items, this.cartItemViewList);

        var totalCostNewValue = computeTotalCost();
        if (totalCostNewValue != this.totalCostOldValue) {
            this.totalCostElm.textContent = formatCurrency(totalCostNewValue);
            this.totalCostOldValue = totalCostNewValue;
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
            div0.id = "carQtcarro";

            dom.div(div1 -> {
                div1.id = "btnCarrinho";

                dom.img(img -> {
                    img.src = "images/carrinho.png";
                });

                dom.header(5, h0 -> {
                    h0.textContent = "Carrinho";
                });

                dom.header(6, h0 -> {
                    this.itemSizeElm = h0;
                    this.itemSizeElm.textContent = "[" + this.state.items.size() + "]";
                    this.itemSizeOldValue = this.state.items.size();
                });
            });

            dom.header(2, h0 -> {
                h0.textContent = "LISTA DE PRODUTOS";
            });
        });

        dom.div(div0 -> {
            div0.id = "carrecibo";

            dom.div(div1 -> div1.id = "carreciboTopo");

            dom.div(div1 -> {
                div1.id = "carcel1";
                div1.textContent = "ITEM";
            });

            dom.div(div1 -> {
                div1.id = "carcel1";
                div1.textContent = "VALOR";
            });

            dom.div(div1 -> {
                div1.id = "carcel1";
                div1.textContent = "QUANTIDADE";
            });

            dom.div(div1 -> {
                this.itemsSlot = newListSlot(div1, this::newItemView, this::updateItem);
            });

            dom.div(div1 -> {
                div1.id = "carvalorcompra";

                dom.span(span0 -> {
                    span0.textContent = "VALOR TOTAL:";
                });

                dom.span(span0 -> {
                    var totalCostNewValue = computeTotalCost();

                    this.totalCostElm = span0;
                    this.totalCostElm.textContent = formatCurrency(totalCostNewValue);
                    this.totalCostOldValue = totalCostNewValue;
                });
            });
        });

        dom.div(div0 -> {
            this.errorElm = div0;
            this.errorElm.id = "error";
            this.errorElm.style.display = this.state.errorCode != 0 ? "block" : "none";
            this.errorElm.textContent = this.state.errorMessage;
        });

        dom.div(div0 -> {
            div0.id = "finalizacompra";

            dom.button(button0 -> {
                button0.onclick = this::emitCommitClicked;

                dom.anchor(a0 -> {
                    a0.className = "link";
                    a0.href = "javascript:void(0)";
                    a0.textContent = "FINALIZAR PEDIDO";
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
                    a0.textContent = "VOLTAR";
                });
            });
        });
    }

    private double computeTotalCost() {
        return this.state.items.stream().mapToDouble(v -> v.price * v.quantity).sum();
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getDecimalFormat().format(value);
    }

    private CartItemViewGwt newItemView() {
        return new CartItemViewGwt(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(CartItemViewGwt itemView, CartItem state) {
        itemView.setState(state, false);
        itemView.doUpdate();
    }

    private Object emitBackClicked(Event evt) {
        this.presenter.onOpenProducts();
        return null;
    }

    private Object emitCommitClicked(Event evt) {
        this.presenter.onBuy();
        return null;
    }

}
