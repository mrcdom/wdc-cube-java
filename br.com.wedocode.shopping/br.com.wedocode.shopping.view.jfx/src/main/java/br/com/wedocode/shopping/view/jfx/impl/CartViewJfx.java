package br.com.wedocode.shopping.view.jfx.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.cart.CartItemViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import br.com.wedocode.shopping.view.jfx.util.ResourceCatalog;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CartViewJfx extends AbstractViewJfx<CartPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    protected CartPresenter.CartState state;

    private boolean notRendered = true;

    private List<CartItemViewJfx> cartItemViewList;

    private BiConsumer<List<CartItem>, List<CartItemViewJfx>> itemsSlot;

    private int itemIdx;

    private Label itemSizeElm;

    private int itemSizeOldValue;

    private Label totalCostElm;

    private double totalCostOldValue;

    private Label errorElm;

    public CartViewJfx(ShoppingJfxApplication app, CartPresenter presenter) {
        super(VIEW_ID, app, presenter, new VBox());
        this.state = presenter.getState();
        this.cartItemViewList = new ArrayList<>();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        if (this.itemSizeOldValue != this.state.items.size()) {
            this.itemSizeElm.setText("[" + this.state.items.size() + "]");
            this.itemSizeOldValue = this.state.items.size();
        }

        this.itemsSlot.accept(this.state.items, this.cartItemViewList);

        var totalCostNewValue = computeTotalCost();
        if (totalCostNewValue != this.totalCostOldValue) {
            this.totalCostElm.setText(formatCurrency(totalCostNewValue));
            this.totalCostOldValue = totalCostNewValue;
        }

        var newErrorDisplay = false;
        var newErrorMessage = "";
        if (this.state.errorCode != 0) {
            newErrorDisplay = true;
            newErrorMessage = this.state.errorMessage;
            this.state.errorCode = 0;
            this.state.errorMessage = null;
        }

        if (!Objects.equals(this.errorElm.getText(), newErrorMessage)) {
            this.errorElm.setText(newErrorMessage);
        }

        if (this.errorElm.isVisible() != newErrorDisplay) {
            this.errorElm.setVisible(newErrorDisplay);
            this.errorElm.setManaged(newErrorDisplay);
        }
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("cart-form");

        dom.hbox(pane1 -> {
            dom.hbox(pane2 -> {
                pane2.getStyleClass().add("cart-logo-pane");
                VBox.setVgrow(pane2, Priority.NEVER);

                dom.img(img -> {
                    img.setImage(ResourceCatalog.getImage("images/carrinho.png"));
                });

                dom.label(label -> {
                    label.getStyleClass().add("lbl");
                    label.setText("Carrinho");
                });

                dom.label(label -> {
                    label.getStyleClass().add("qtd");
                    this.itemSizeElm = label;
                    this.itemSizeElm.setText("[" + this.state.items.size() + "]");
                    this.itemSizeOldValue = this.state.items.size();
                });
            });

            dom.hSpacer();
        });

        dom.label(label -> {
            label.getStyleClass().add("title");
            label.setText("LISTA DE PRODUTOS");
        });

        dom.vbox(pane1 -> {
            pane1.getStyleClass().add("content");

            dom.hbox(pane2 -> {
                pane2.getStyleClass().add("header");

                dom.label(label -> {
                    label.getStyleClass().add("cell-1");
                    label.setText("ITEM");
                });

                dom.label(label -> {
                    label.getStyleClass().add("cell-2");
                    label.setText("VALOR");
                });

                dom.label(label -> {
                    label.getStyleClass().add("cell-3");
                    label.setText("QUANTIDADE");
                });
            });

            dom.vbox(pane2 -> {
                pane2.getStyleClass().add("tbody");
                this.itemsSlot = newListSlot(pane2, this::newItemView, this::updateItem);
            });

            dom.hbox(pane2 -> {
                pane2.getStyleClass().add("footer");

                dom.hSpacer();

                dom.label(span0 -> {
                    span0.setText("VALOR TOTAL: ");
                });

                dom.label(label -> {
                    var totalCostNewValue = computeTotalCost();

                    this.totalCostElm = label;
                    this.totalCostElm.setText(formatCurrency(totalCostNewValue));
                    this.totalCostOldValue = totalCostNewValue;
                });
            });
        });

        dom.hbox(pane1 -> {
            pane1.getStyleClass().add("error");

            dom.label(label -> {
                this.errorElm = label;
                this.errorElm.setVisible(this.state.errorCode != 0);
                this.errorElm.setManaged(this.state.errorCode != 0);
                this.errorElm.setText(this.state.errorMessage);
            });
        });

        dom.hbox(pane1 -> {
            dom.hSpacer();

            dom.button(button -> {
                button.getStyleClass().add("do-buy-button");
                button.setText("FINALIZAR PEDIDO");
                button.setOnAction(this::emitCommitClicked);
            });
        });

        dom.button(button -> {
            button.getStyleClass().add("back-button");
            button.setText("< VOLTAR");
            button.setOnAction(this::emitBackClicked);
        });
    }

    private double computeTotalCost() {
        return this.state.items.stream().mapToDouble(v -> v.price * v.quantity).sum();
    }

    private String formatCurrency(double value) {
        return "R$ " + NumberFormat.getInstance().format(value);
    }

    private CartItemViewJfx newItemView() {
        return new CartItemViewJfx(this.app, this.presenter, this.itemIdx++);
    }

    private void updateItem(CartItemViewJfx itemView, CartItem state) {
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
