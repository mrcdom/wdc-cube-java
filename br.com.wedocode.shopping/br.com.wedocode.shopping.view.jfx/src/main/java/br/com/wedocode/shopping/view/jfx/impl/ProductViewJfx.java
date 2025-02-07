package br.com.wedocode.shopping.view.jfx.impl;

import java.text.NumberFormat;
import java.util.Objects;
import java.util.Stack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.util.StringUtils;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import br.com.wedocode.shopping.view.jfx.util.ResourceCatalog;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.converter.NumberStringConverter;

public class ProductViewJfx extends AbstractViewJfx<ProductPresenter> {

    private static Logger LOG = LoggerFactory.getLogger(ProductViewJfx.class);

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    public ProductPresenter.ProductState state;

    private boolean notRendered = true;

    private Text nameElm1;

    private Label nameElm2;

    private String nameOldValue;

    private ImageView imageElm;

    private String imageOldValue;

    private Label priceElm;

    private double priceOldValue;

    private TextField quantityElm;

    private TextFlow descriptionElm;

    private String descriptionOldValue;

    private Label errorElm;

    public ProductViewJfx(ShoppingJfxApplication app, ProductPresenter presenter) {
        super(VIEW_ID, app, presenter, new VBox());
        this.state = presenter.getState();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.nameOldValue, this.state.product.name)) {
            this.nameElm1.setText(this.state.product.name);
            this.nameElm2.setText(this.state.product.name);
            this.nameOldValue = this.state.product.name;
        }

        if (!Objects.equals(this.imageOldValue, this.state.product.image)) {
            this.imageElm.setImage(ResourceCatalog.getImage(this.state.product.image));
            this.imageOldValue = this.state.product.image;
        }

        if (this.priceOldValue != this.state.product.price) {
            this.priceElm.setText(formatCurrency(this.state.product.price));
            this.priceOldValue = this.state.product.price;
        }

        if (!Objects.equals(this.descriptionOldValue, this.state.product.description)) {
            this.renderHtml(this.descriptionElm, this.state.product.description);
            this.descriptionOldValue = this.state.product.description;
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
        }
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("product-form");

        // Breadcrambs
        dom.textFlow(pane1 -> {
            pane1.getStyleClass().add("breadcrambs");

            dom.text(label -> {
                label.setText("Produtos > ");
            });

            dom.text(label -> {
                this.nameElm1 = label;
                this.nameElm1.setText(this.state.product.name);
                this.nameOldValue = this.state.product.name;
            });
        });

        // Product
        dom.hbox(pane1 -> {

            dom.vbox(pane2 -> {
                dom.img(img -> {
                    this.imageElm = img;
                    this.imageElm.setImage(ResourceCatalog.getImage(this.state.product.image));
                    this.imageOldValue = this.state.product.image;
                });

                dom.vSpacer();

                dom.button(button0 -> {
                    button0.getStyleClass().add("back-button");

                    button0.setText("< VOLTAR");
                    button0.setOnAction(this::emitBackClicked);
                });
            });

            dom.vbox(pane2 -> {
                HBox.setHgrow(pane2, Priority.ALWAYS);

                pane2.getStyleClass().add("content");

                dom.label(label -> {
                    label.getStyleClass().add("lbl-name-val");

                    this.nameElm2 = label;
                    this.nameElm2.setText(this.state.product.name);
                });

                dom.hbox(pane3 -> {
                    pane3.getStyleClass().add("pane-price-qtd");

                    dom.label(label -> {
                        label.getStyleClass().add("lbl-price-val");

                        this.priceElm = label;
                        this.priceElm.setText(formatCurrency(this.state.product.price));
                        this.priceOldValue = this.state.product.price;
                    });

                    dom.hSpacer();

                    dom.label(label -> label.setText("Quantidade:"));

                    dom.textField(field -> {
                        field.getStyleClass().add("fld-price");
                        field.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));

                        this.quantityElm = field;
                        this.quantityElm.setText("1");
                    });
                });

                dom.label(label -> {
                    label.getStyleClass().add("description-title");
                    label.setText("DESCRIÇÃO DO PRODUTO");
                });

                dom.textFlow(pane3 -> {
                    pane3.getStyleClass().add("description");

                    this.descriptionElm = pane3;
                    this.renderHtml(this.descriptionElm, this.state.product.description);
                    this.descriptionOldValue = this.state.product.description;
                });

                dom.label(label -> {
                    this.errorElm = label;
                    this.errorElm.setVisible(this.state.errorCode != 0);
                    this.errorElm.setText(this.state.errorMessage);
                });

                dom.hbox(pane3 -> {
                    pane3.setAlignment(Pos.CENTER);

                    dom.button(button0 -> {
                        button0.getStyleClass().add("buy-button");

                        button0.setText("COMPRAR");
                        button0.setOnAction(this::emitBuyClicked);
                    });
                });
            });
        });
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    private void emitBackClicked(ActionEvent evt) {
        this.presenter.onOpenProducts().finally_(this::update);
    }

    private void emitBuyClicked(ActionEvent evt) {
        var quantity = 1;
        try {
            quantity = Integer.parseInt(this.quantityElm.getText());
        } catch (NumberFormatException caught) {
            LOG.error("Trying to parse value: " + this.quantityElm.getText(), caught);
        }
        this.presenter.onAddToCart(quantity).finally_(this::update);
    }

    private void renderHtml(Pane pane, String htmlString) {
        pane.getChildren().removeIf(v -> true);

        var doc = Jsoup.parseBodyFragment(htmlString);
        doc.body().traverse(new VerySimpleHtmlRenderer(pane));
    }

    private static class VerySimpleHtmlRenderer implements NodeVisitor {

        Stack<Pane> stack;

        VerySimpleHtmlRenderer(Pane pane) {
            this.stack = new Stack<>();
            this.stack.push(pane);
        }

        @Override
        public void head(org.jsoup.nodes.Node node, int depth) {
            var pane = this.stack.peek();

            if (node instanceof TextNode) {
                var htmlNode = (TextNode) node;

                var txt = htmlNode.text();
                if (StringUtils.isNotBlank(txt)) {
                    var lbl = new Text(txt.trim());
                    pane.getChildren().add(lbl);
                }
            } else if (node instanceof org.jsoup.nodes.Element) {
                var htmlElm = (org.jsoup.nodes.Element) node;
                if ("ul".equalsIgnoreCase(htmlElm.tagName())) {
                    var ul = new VBox();
                    ul.getStyleClass().add("ul");
                    pane.getChildren().add(ul);
                    this.stack.add(ul);
                } else if ("li".equalsIgnoreCase(htmlElm.tagName())) {
                    var li = new TextFlow();
                    li.getStyleClass().add("li");
                    li.getChildren().add(new Text("\u2022 "));
                    pane.getChildren().add(li);
                    this.stack.push(li);
                }
            }
        }

        @Override
        public void tail(org.jsoup.nodes.Node node, int depth) {
            if (node instanceof org.jsoup.nodes.Element) {
                this.stack.pop();
            }
        }

    }

}
