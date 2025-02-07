package br.com.wedocode.shopping.view.jfx.impl.restricted;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class RestrictedPurchaseItemViewJfx extends AbstractViewJfx<RestrictedPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private PurchaseInfo state;

    private boolean notRendered = true;

    private Label idElm;

    private long idOldValue;

    private Label dateElm;

    private long dateOldValue;

    private Label itemsElm;

    private String itemsOldValue;

    private Text totalElm;

    private double totalOldValue;

    public RestrictedPurchaseItemViewJfx(ShoppingJfxApplication app, WebFlowPresenter presenter, int idx) {
        super(BASE_VIEW_ID + "-" + idx, app, (RestrictedPresenter) presenter, new VBox());
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
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        if (this.idOldValue != this.state.id) {
            this.idElm.setText("#" + this.idOldValue);
            this.idOldValue = this.state.id;
        }

        if (this.dateOldValue != this.state.date) {
            this.dateElm.setText(getDateStr());
            this.dateOldValue = this.state.date;
        }

        var purchaseItemsNewValue = this.getItemsStr();
        if (!Objects.equals(this.itemsOldValue, purchaseItemsNewValue)) {
            this.itemsElm.setText(purchaseItemsNewValue);
            this.itemsOldValue = purchaseItemsNewValue;
        }

        if (this.totalOldValue != this.state.total) {
            this.totalElm.setText(getTotalValue());
            this.totalOldValue = this.state.total;
        }
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("restricted-purchases-item");

        dom.hbox(pane1 -> {
            pane1.getStyleClass().add("order-pnl");

            dom.label(label -> {
                label.getStyleClass().add("order-lbl");
                label.setText("Compra");
            });

            dom.label(label -> {
                label.getStyleClass().add("order-num");

                this.idElm = label;
                this.idElm.setText("#" + this.idOldValue);
                this.idOldValue = this.state.id;
            });
        });

        dom.vbox(pane1 -> {
            pane1.getStyleClass().add("order-info");

            dom.label(label -> {
                label.getStyleClass().add("order-dt-lbl");
                label.setText("Data da compra:");
            });

            dom.label(label -> {
                label.getStyleClass().add("order-dt-val");
                this.dateElm = label;
                this.dateElm.setText(getDateStr());
                this.dateOldValue = this.state.date;
            });

            dom.label(label -> {
                label.getStyleClass().add("order-items-lbl");
                label.setText("Itens adquiridos:");
            });

            dom.label(label -> {
                label.getStyleClass().add("order-items-val");
                label.setWrapText(true);

                var itemsNewValue = getItemsStr();

                this.itemsElm = label;
                this.itemsElm.setText(itemsNewValue);
                this.itemsOldValue = itemsNewValue;
            });

            dom.textFlow(pane2 -> {
                pane2.getStyleClass().add("order-total");

                dom.text(label -> {
                    label.setText("Valor Total: ");
                });

                dom.text(label -> {
                    this.totalElm = label;
                    this.totalElm.setText(getTotalValue());
                    this.totalOldValue = this.state.total;
                });
            });

            dom.hbox(pane2 -> {
                dom.hSpacer();

                dom.button(button -> {
                    button.setText("VEJA MAIS DETALHES");
                    button.setOnAction(this::emitDetailsClicked);
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
                sb.append("; ");
            }
        }

        return sb.toString();
    }

    private String getTotalValue() {
        return NumberFormat.getCurrencyInstance().format(this.state.total);
    }

    private void emitDetailsClicked(Event evt) {
        this.presenter.onOpenReceipt(this.state.id);
    }

}