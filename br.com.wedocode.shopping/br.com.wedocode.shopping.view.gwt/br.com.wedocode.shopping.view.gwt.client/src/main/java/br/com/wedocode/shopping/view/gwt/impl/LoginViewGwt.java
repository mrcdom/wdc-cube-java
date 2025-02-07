package br.com.wedocode.shopping.view.gwt.impl;

import java.util.Objects;

import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.util.HtmlDom;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;

public class LoginViewGwt extends AbstractViewGwt<LoginPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewGwt.viewIdGen++);

    protected LoginPresenter.LoginState state;

    private boolean notRendered = true;

    private HTMLInputElement pwd;

    private HTMLInputElement usr;

    private HTMLDivElement errorDiv;

    public LoginViewGwt(ShoppingApplicationGwt app, LoginPresenter presenter) {
        super(VIEW_ID, app, presenter);
        this.element.className = "center";
        this.state = presenter.getState();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            HtmlDom.render(this.element, this::initialRender);
            this.notRendered = false;
        }

        var errorDisplay = "none";
        var errorMessage = "";
        if (this.state.errorCode != 0) {
            errorDisplay = "block";
            errorMessage = this.state.errorMessage;
            this.state.errorCode = 0;
            this.state.errorMessage = null;
        }

        if (!Objects.equals(this.errorDiv.textContent, errorMessage)) {
            this.errorDiv.textContent = errorMessage;
        }

        if (!Objects.equals(this.errorDiv.style.display, errorDisplay)) {
            this.errorDiv.style.display = errorDisplay;
        }
    }

    private void initialRender(HtmlDom dom, HTMLDivElement viewDiv) {
        dom.div(div1 -> {
            div1.id = "formulario";

            dom.img(img -> {
                img.id = "logo";
                img.src = "images/big_logo.png";
                img.alt = "WeDoCode Shopping";
            });

            dom.div(div2 -> {
                div2.id = "campos";

                dom.paragraph(p -> {
                    p.textContent = "Usuário";
                });

                dom.input(input -> {
                    input.type = "text";
                    input.name = "usr";
                    this.usr = input;
                });

                dom.paragraph(p -> {
                    p.textContent = "Senha";
                });

                dom.input(input -> {
                    input.type = "password";
                    input.name = "pwd";
                    input.onkeydown = this::emitOnPwdKeyDown;
                    this.pwd = input;
                });

                dom.div(div3 -> {
                    div3.id = "login";
                    dom.button(button0 -> {
                        dom.anchor(a -> {
                            a.className = "link";
                            a.innerHTML = "&nbsp;LOGIN&nbsp;";
                        });

                        button0.onclick = this::emitLogin;
                    });
                });

                dom.div(div3 -> {
                    div3.className = "error";
                    div3.style.display = this.state.errorCode != 0 ? "block" : "none";
                    div3.textContent = this.state.errorMessage;
                    this.errorDiv = div3;
                });
            });
        });
    }

    private void populateState() {
        this.state.userName = this.usr.value;
        this.state.password = this.pwd.value;
    }

    private Object emitOnPwdKeyDown(Event evt) {
        var event = (KeyboardEvent) evt;

        if ("Enter".equalsIgnoreCase(event.code)) {
            populateState();
            this.presenter.onEnter();
        }

        return null;
    }

    private Object emitLogin(Event evt) {
        populateState();
        this.presenter.onEnter();
        return null;
    }

}
