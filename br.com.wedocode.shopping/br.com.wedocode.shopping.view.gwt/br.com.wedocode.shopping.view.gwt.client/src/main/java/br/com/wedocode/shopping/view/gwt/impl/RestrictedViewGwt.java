package br.com.wedocode.shopping.view.gwt.impl;

import java.util.Objects;
import java.util.function.Consumer;

import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.impl.restricted.RestrictedDefaultViewGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public class RestrictedViewGwt extends AbstractViewGwt<RestrictedPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    public RestrictedPresenter.RestrictedState state;

    private boolean notRendered = true;

    private HTMLElement nickNameElm;

    private String nickNameOldValue;

    private HTMLElement cartItemCountElm;

    private int cartItemCountOldValue;

    private Consumer<HTMLElement> contentSlot;

    private RestrictedDefaultViewGwt defaultView;

    public RestrictedViewGwt(ShoppingApplicationGwt app, RestrictedPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.state = presenter.getState();
        this.defaultView = new RestrictedDefaultViewGwt(app, this.presenter);
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        if (!Objects.equals(this.nickNameOldValue, this.state.nickName)) {
            this.nickNameElm.textContent = this.state.nickName;
            this.nickNameOldValue = this.state.nickName;
        }

        if (this.cartItemCountOldValue != this.state.cartItemCount) {
            this.cartItemCountElm.textContent = "[" + this.state.cartItemCount + "]";
            this.cartItemCountOldValue = this.state.cartItemCount;
        }

        var contentView = (AbstractViewGwt<?>) this.state.contentView;
        if (contentView != null) {
            this.contentSlot.accept(contentView.element);
        } else {
            // It is needed because this view is not known by presenter
            this.defaultView.doUpdate();
            this.contentSlot.accept(this.defaultView.element);
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        this.element.style.display = "flex";
        this.element.style.flexDirection = "column";

        dom.div(div0 -> {
            div0.id = "header";
            div0.style.alignSelf = "stretch";

            dom.div(div1 -> {
                div1.className = "left";

                dom.img(img -> {
                    img.id = "logo";
                    img.src = "images/logo.png";
                    img.alt = "WeDoCode Shopping";
                });
            });

            dom.div(div1 -> {
                div1.className = "right";

                dom.div(div2 -> {
                    div2.id = "btnCarrinho";

                    dom.anchor(a -> {
                        a.href = "javascript:void(0)";
                        a.onclick = this::emitOpenCart;

                        dom.img(img -> {
                            img.src = "images/carrinho.png";
                            img.alt = "WeDoCode Shopping";
                        });

                        dom.header(5, h0 -> {
                            h0.textContent = "Carrinho";
                        });

                        dom.header(6, h0 -> {
                            h0.textContent = "[" + this.state.cartItemCount + "]";
                            this.cartItemCountOldValue = this.state.cartItemCount;
                            this.cartItemCountElm = h0;
                        });
                    });
                });
            });
        });

        dom.div(div0 -> {
            div0.id = "grandec";

            div0.style.alignSelf = "center";

            div0.style.display = "flex";
            div0.style.flexDirection = "column";

            dom.div(div1 -> {
                div1.id = "boasvindas";

                dom.paragraph(p0 -> {
                    dom.span(span0 -> {
                        dom.span(span1 -> {
                            span1.textContent = "Seja bem vindo, ";
                        });

                        dom.bold(b0 -> {
                            b0.textContent = this.state.nickName;
                            this.nickNameOldValue = this.state.nickName;
                            this.nickNameElm = b0;
                        });

                        dom.span(span1 -> {
                            span1.textContent = "!";
                        });
                    });

                    dom.span(span0 -> {
                        span0.id = "sair2";

                        dom.button(button0 -> {
                            button0.onclick = this::onExit;

                            dom.anchor(a0 -> {
                                a0.textContent = "sair";
                            });
                        });
                    });
                });

            });

            dom.div(div1 -> {
                this.contentSlot = newOneSlot(div1);
            });
        });
    }

    private Object emitOpenCart(Event evt) {
        this.presenter.onOpenCart();
        return null;
    }

    private Object onExit(Event evt) {
        this.presenter.onExit();
        return null;
    }

}
