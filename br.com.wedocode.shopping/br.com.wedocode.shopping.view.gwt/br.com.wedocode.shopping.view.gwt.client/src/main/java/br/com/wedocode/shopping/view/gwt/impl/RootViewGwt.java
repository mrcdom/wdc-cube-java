package br.com.wedocode.shopping.view.gwt.impl;

import java.util.Objects;
import java.util.function.Consumer;

import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.util.DomFactory;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class RootViewGwt extends AbstractViewGwt<RootPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    protected RootPresenter.RootState state;

    protected HTMLDivElement messageElm;

    protected Consumer<HTMLElement> contentSlot;

    public RootViewGwt(ShoppingApplicationGwt app, RootPresenter presenter) {
        super(VIEW_ID, app, presenter, app.getMainDiv());
        this.contentSlot = newOneSlot(this.element);
        this.state = presenter.getState();
    }

    @Override
    protected void doUpdate() {
        if (this.state.errorMessage != null) {
            if (this.messageElm == null) {
                this.messageElm = DomFactory.newDiv();
                this.messageElm.className = "error";
            }

            if (!Objects.equals(this.state.errorMessage, this.messageElm.textContent)) {
                this.messageElm.textContent = this.state.errorMessage;
            }

            this.contentSlot.accept(this.messageElm);
        } else {
            var contentView = (AbstractViewGwt<?>) this.state.contentView;
            if (contentView != null) {
                this.contentSlot.accept(contentView.element);
            } else {
                this.contentSlot.accept(null);
            }
        }
    }

}
