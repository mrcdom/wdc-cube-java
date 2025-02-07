package br.com.wedocode.shopping.view.gwt.impl.restricted;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gwt.i18n.client.NumberFormat;

import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.AbstractViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class RestrictedPurchaseItemViewGwt extends AbstractViewGwt<RestrictedPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    private PurchaseInfo state;

    private boolean notRendered = true;

    private HTMLElement idElm;

    private long idOldValue;

    private HTMLElement dateElm;

    private long dateOldValue;

    private HTMLElement itemsElm;

    private String itemsOldValue;

    private HTMLElement totalElm;

    private double totalOldValue;

    public RestrictedPurchaseItemViewGwt(ShoppingApplicationGwt app, WebFlowPresenter presenter, int idx) {
        super(BASE_VIEW_ID + "-" + idx, app, (RestrictedPresenter) presenter);
    }

    public void setState(PurchaseInfo state) {
        this.setState(state, true);
    }

    public void setState(PurchaseInfo state, boolean scheduleUpdate) {
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

        if (this.idOldValue != this.state.id) {
            this.idElm.textContent = String.valueOf(this.idOldValue);
            this.idOldValue = this.state.id;
        }

        if (this.dateOldValue != this.state.date) {
            this.dateElm.textContent = getDateStr();
            this.dateOldValue = this.state.date;
        }

        var purchaseItemsNewValue = this.getItemsStr();
        if (!Objects.equals(this.itemsOldValue, purchaseItemsNewValue)) {
            this.itemsElm.textContent = purchaseItemsNewValue;
            this.itemsOldValue = purchaseItemsNewValue;
        }

        if (this.totalOldValue != this.state.total) {
            this.totalElm.textContent = getTotalValue();
            this.totalOldValue = this.state.total;
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        viewDiv.id = "hbox";

        dom.header(1, h0 -> {
            dom.span(span0 -> {
                span0.textContent = "Compra #";
            });

            dom.span(span0 -> {
                this.idElm = span0;
                this.idElm.textContent = String.valueOf(this.idOldValue);
                this.idOldValue = this.state.id;
            });
        });

        dom.header(2, h0 -> {
            h0.textContent = "Data da compra:";
        });

        dom.paragraph(p0 -> {
            this.dateElm = p0;
            this.dateElm.textContent = getDateStr();
            this.dateOldValue = this.state.date;
        });

        dom.header(2, h0 -> {
            h0.textContent = "Itens adquiridos:";
        });

        dom.paragraph(p0 -> {
            var itemsNewValue = getItemsStr();

            this.itemsElm = p0;
            this.itemsElm.textContent = itemsNewValue;
            this.itemsOldValue = itemsNewValue;
        });

        dom.paragraph(p0 -> {
            dom.bold(b0 -> {
                b0.textContent = "Valor Total: ";
            });

            dom.span(span0 -> {
                this.totalElm = span0;
                this.totalElm.textContent = getTotalValue();
                this.totalOldValue = this.state.total;
            });
        });

        dom.div(div1 -> {
            div1.id = "vermais";

            dom.button(button0 -> {
                button0.onclick = this::emitDetailsClicked;

                dom.anchor(a0 -> {
                    a0.className = "link";
                    a0.href = "javascript:void(0)";
                    a0.textContent = "VEJA MAIS DETALHES";
                });
            });
        });
    }

    private String getDateStr() {
        var date = LocalDate.ofEpochDay(TimeUnit.DAYS.convert(this.state.date, TimeUnit.MILLISECONDS));
        return date.toString();
    }

    private String getItemsStr() {
        var sb = new StringBuilder();

        for (int i = 0, iLast = this.state.items.size() - 1; i <= iLast; i++) {
            var iten = this.state.items.get(i);
            sb.append(iten);
            if (i < iLast) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    private String getTotalValue() {
        return "R$ " + NumberFormat.getDecimalFormat().format(this.state.total);
    }

    private Object emitDetailsClicked(Event evt) {
        this.presenter.onOpenReceipt(this.state.id);
        return null;
    }

}