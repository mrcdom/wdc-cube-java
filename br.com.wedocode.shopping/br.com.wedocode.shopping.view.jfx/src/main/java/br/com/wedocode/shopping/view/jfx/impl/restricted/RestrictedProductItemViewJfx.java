package br.com.wedocode.shopping.view.jfx.impl.restricted;

import java.text.NumberFormat;
import java.util.Objects;

import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.impl.AbstractViewJfx;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import br.com.wedocode.shopping.view.jfx.util.ResourceCatalog;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class RestrictedProductItemViewJfx extends AbstractViewJfx<RestrictedPresenter> {

    private static final String BASE_VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    private ProductItem state;

    private boolean notRendered = true;

    private ImageView imageElm;

    private String imageOldValue;

    private Label nameElm;

    private String nameOldValue;

    private Label priceElm;

    private double priceOldValue;

    public RestrictedProductItemViewJfx(ShoppingJfxApplication app, WebFlowPresenter presenter, int idx) {
        super(BASE_VIEW_ID + "-" + idx, app, (RestrictedPresenter) presenter, new StackPane());
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
            JfxDom.render((StackPane) this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.imageOldValue, this.state.image)) {
            this.imageElm.setImage(ResourceCatalog.getImage(this.state.image));
            this.imageOldValue = this.state.image;
        }

        if (!Objects.equals(this.nameOldValue, this.state.name)) {
            this.nameElm.setText(this.state.name);
            this.nameOldValue = this.state.name;
        }

        if (this.priceOldValue != this.state.price) {
            this.priceElm.setText(getPriceStr());
            this.priceOldValue = this.state.price;
        }
    }

    private void initialRender(JfxDom dom, StackPane pane0) {
        dom.vbox(pane1 -> {
            StackPane.setMargin(pane1, new Insets(10));

            pane1.getStyleClass().add("product-selection-item");

            pane1.setOnMouseClicked(this::emitClicked);

            dom.img(img -> {
                img.getStyleClass().add("image");

                img.setFitWidth(194);
                img.setFitHeight(152);

                this.imageElm = img;
                this.imageElm.setImage(ResourceCatalog.getImage(this.state.image));
                this.imageOldValue = this.state.image;
            });

            dom.vbox(pane2 -> {
                pane2.getStyleClass().add("label-group");

                dom.label(label -> {
                    label.getStyleClass().add("label-name");

                    this.nameElm = label;
                    this.nameElm.setText(this.state.name);
                    this.nameOldValue = this.state.name;
                });

                dom.label(label -> {
                    label.getStyleClass().add("label-price");

                    this.priceElm = label;
                    this.priceElm.setText(getPriceStr());
                    this.priceOldValue = this.state.price;
                });
            });
        });
    }

    private String getPriceStr() {
        return "R$ " + NumberFormat.getInstance().format(this.state.price);
    }

    private void emitClicked(MouseEvent evt) {
        this.presenter.onOpenProduct(this.state.id);
    }

}