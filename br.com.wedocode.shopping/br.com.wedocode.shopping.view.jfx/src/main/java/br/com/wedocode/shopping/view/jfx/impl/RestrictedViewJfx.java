package br.com.wedocode.shopping.view.jfx.impl;

import java.util.Objects;
import java.util.function.Consumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.restricted.RestrictedDefaultViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import br.com.wedocode.shopping.view.jfx.util.ResourceCatalog;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class RestrictedViewJfx extends AbstractViewJfx<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    public RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private Text nickNameElm;

    private String nickNameOldValue;

    private Label cartItemCountElm;

    private int cartItemCountOldValue;

    private Consumer<Node> contentSlot;

    private RestrictedDefaultViewJfx defaultView;

    public RestrictedViewJfx(ShoppingJfxApplication app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter, new VBox());
        this.state = presenter.getState();
        this.defaultView = new RestrictedDefaultViewJfx(app, this.presenter);
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.nickNameOldValue, this.state.nickName)) {
            this.nickNameElm.setText(this.state.nickName);
            this.nickNameOldValue = this.state.nickName;
        }

        if (this.cartItemCountOldValue != this.state.cartItemCount) {
            this.cartItemCountElm.setText("[" + this.state.cartItemCount + "]");
            this.cartItemCountOldValue = this.state.cartItemCount;
        }

        var contentView = (AbstractViewJfx<?>) this.state.contentView;
        if (contentView != null) {
            this.contentSlot.accept(contentView.element);
        } else {
            // It is needed because this view is not known by presenter
            this.defaultView.doUpdate();
            this.contentSlot.accept(this.defaultView.element);
        }
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("restricted-main");

        // Header
        dom.hbox(pane1 -> {
            pane1.getStyleClass().add("restricted-header");

            dom.img(img -> {
                img.setImage(ResourceCatalog.getImage("images/logo.png"));
            });

            dom.hSpacer();

            dom.hbox(pane2 -> {
                pane2.getStyleClass().add("restricted-header-cart");

                pane2.setOnMouseClicked(this::emitOpenCart);

                dom.img(img -> {
                    img.setImage(ResourceCatalog.getImage("images/carrinho.png"));
                });

                dom.label(label -> {
                    label.getStyleClass().add("lbl");

                    label.setText("Carrinho");
                });

                dom.label(label -> {
                    label.getStyleClass().add("qtd");

                    this.cartItemCountElm = label;
                    this.cartItemCountElm.setText("[" + this.state.cartItemCount + "]");
                    this.cartItemCountOldValue = this.state.cartItemCount;
                });
            });
        });

        dom.vbox(pane1 -> {
            pane1.getStyleClass().add("restricted-body");

            dom.textFlow(pane2 -> {
                pane2.getStyleClass().add("restricted-welcome");

                dom.text(span1 -> {
                    span1.getStyleClass().add("welcome");
                    span1.setText("Seja bem vindo, ");
                });

                dom.text(label -> {
                    label.getStyleClass().add("name");

                    this.nickNameElm = label;
                    this.nickNameElm.setText(this.state.nickName);
                    this.nickNameOldValue = this.state.nickName;
                });

                dom.text(label -> {
                    label.setText("!");
                });

                dom.hSpacer(10);

                dom.button(button -> {
                    button.setText("sair");
                    button.setOnAction(this::onExit);
                });
            });

            // ContentSlot
            dom.stackPane(pane2 -> {
                this.contentSlot = newOneSlot(pane2);
            });
        });
    }

    private void emitOpenCart(MouseEvent evt) {
        this.presenter.onOpenCart();
    }

    private void onExit(ActionEvent evt) {
        this.presenter.onExit();
    }

}
