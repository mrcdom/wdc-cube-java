package br.com.wedocode.shopping.view.gwt.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptItem;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.receipt.ReceiptItemViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class ReceiptViewGwt extends AbstractViewGwt<ReceiptPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    public ReceiptPresenter.ReceiptState state;

    private boolean notRendered = true;

    private HTMLElement notifySuccessElm;

    private HTMLElement totalElm;

    private double totalOldValue;

    private List<ReceiptItemViewGwt> receiptItemViewList;

    private BiConsumer<List<ReceiptItem>, List<ReceiptItemViewGwt>> itemsSlot;

    private int itemIdx;

    public ReceiptViewGwt(ShoppingApplicationGwt app, ReceiptPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
        this.receiptItemViewList = new ArrayList<>();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        var notifySuccessDisplay = "none";
        if (this.state.notifySuccess) {
            notifySuccessDisplay = "block";
            this.state.notifySuccess = false;
        }

        if (!Objects.equals(notifySuccessDisplay, this.notifySuccessElm.style.display)) {
            this.notifySuccessElm.style.display = notifySuccessDisplay;
        }

        if (this.totalOldValue != this.state.receipt.total) {
            this.totalElm.textContent = formatCurrency(this.state.receipt.total);
            this.totalOldValue = this.state.receipt.total;
        }

        this.itemsSlot.accept(this.state.receipt.items, this.receiptItemViewList);
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.id = "boxProdutos";

        dom.header(1, h0 -> {
            this.notifySuccessElm = h0;
            this.notifySuccessElm.style.display = this.state.notifySuccess ? "block" : "none";
            this.notifySuccessElm.textContent = "COMPRA EFETUADA COM SUCESSO";
        });

        dom.header(2, h0 -> {
            h0.textContent = "IMPRIMA SEU RECIBO:";
        });

        dom.div(div1 -> {
            div1.id = "recibo";

            dom.div(div2 -> {
                div2.id = "reciboTopo";

                dom.header(3, h0 -> h0.textContent = "WEDOCODE SHOPPING - SUA COMPRA CERTA NA INTERNET");
                dom.header(3, h0 -> h0.textContent = "Recibo de compra");
            });

            dom.div(div2 -> {
                div2.id = "cel1";
                div2.textContent = "ITEM";
            });

            dom.div(div2 -> {
                div2.id = "cel1";
                div2.textContent = "VALOR";
            });

            dom.div(div2 -> {
                div2.id = "cel1";
                div2.textContent = "QUANTIDADE";
            });

            dom.div(div2 -> {
                this.itemsSlot = newListSlot(div2, this::newItemView, this::updateItem);
            });

            dom.div(div2 -> div2.id = "borda2");
            dom.div(div2 -> div2.id = "borda");

            dom.div(div2 -> {
                div2.id = "valorcompra";

                dom.span(span0 -> span0.textContent = "VALOR TOTAL:");

                dom.span(span0 -> {
                    this.totalElm = span0;
                    this.totalElm.textContent = formatCurrency(this.state.receipt.total);
                    this.totalOldValue = this.state.receipt.total;
                });
            });
        });

        dom.div(div1 -> {
            div1.id = "naveg";

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

    private ReceiptItemViewGwt newItemView() {
        return new ReceiptItemViewGwt(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(ReceiptItemViewGwt itemView, ReceiptItem state) {
        itemView.setState(state, false);
        itemView.doUpdate();
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getDecimalFormat().format(value);
    }

    private Object emitBackClicked(Event evt) {
        this.presenter.onOpenProducts();
        return null;
    }

}
