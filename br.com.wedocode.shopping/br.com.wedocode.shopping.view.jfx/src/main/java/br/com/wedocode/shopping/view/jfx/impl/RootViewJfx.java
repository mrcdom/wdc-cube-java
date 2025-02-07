package br.com.wedocode.shopping.view.jfx.impl;

import java.util.Objects;
import java.util.function.Consumer;

import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class RootViewJfx extends AbstractViewJfx<RootPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    protected RootPresenter.RootState state;

    protected ErrorPane messageElm;

    protected Consumer<Node> contentSlot;

    public RootViewJfx(ShoppingJfxApplication app, RootPresenter presenter) {
        super(VIEW_ID, app, presenter, app.getRootElement());
        this.contentSlot = newOneSlot(this.element);
        this.state = presenter.getState();
    }

    @Override
    protected void doUpdate() {
        if (this.state.errorMessage != null) {
            if (this.messageElm == null) {
                this.messageElm = new ErrorPane();
            }

            if (!Objects.equals(this.state.errorMessage, this.messageElm.getTextContent())) {
                this.messageElm.setTextContent(this.state.errorMessage);
            }

            this.contentSlot.accept(this.messageElm);
        } else {
            var contentView = (AbstractViewJfx<?>) this.state.contentView;
            if (contentView != null) {
                this.contentSlot.accept(contentView.element);
            } else {
                this.contentSlot.accept(null);
            }
        }
    }

    private static class ErrorPane extends StackPane {

        private Label label;

        public ErrorPane() {
            super();

            this.label = new Label();
            this.getChildren().add(this.label);
        }

        public void setTextContent(String text) {
            this.label.setText(text);
        }

        public String getTextContent() {
            return this.label.getText();
        }

    }
}
