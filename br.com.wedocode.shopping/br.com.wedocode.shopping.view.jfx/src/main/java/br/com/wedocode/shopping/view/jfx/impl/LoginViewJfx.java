package br.com.wedocode.shopping.view.jfx.impl;

import java.util.Objects;

import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.util.JfxDom;
import br.com.wedocode.shopping.view.jfx.util.ResourceCatalog;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class LoginViewJfx extends AbstractViewJfx<LoginPresenter> {

    private static final String VIEW_ID = String.valueOf(AbstractViewJfx.viewIdGen++);

    protected LoginPresenter.LoginState state;

    protected boolean notRendered = true;

    protected PasswordField pwd;

    protected TextField usr;

    protected Label errorDiv;

    public LoginViewJfx(ShoppingJfxApplication app, LoginPresenter presenter) {
        super(VIEW_ID, app, presenter, new VBox());
        this.state = presenter.getState();
    }

    @Override
    protected void doUpdate() {
        if (this.notRendered) {
            JfxDom.render((VBox) this.element, this::initialRender);
            this.notRendered = false;
        }

        var errorVisible = false;
        var errorMessage = "";
        if (this.state.errorCode != 0) {
            errorVisible = true;
            errorMessage = this.state.errorMessage;
            this.state.errorCode = 0;
            this.state.errorMessage = null;
        }

        if (!Objects.equals(this.errorDiv.getText(), errorMessage)) {
            this.errorDiv.setText(errorMessage);
        }

        if (this.errorDiv.isVisible() != errorVisible) {
            this.errorDiv.setVisible(errorVisible);
        }
    }

    private void initialRender(JfxDom dom, VBox pane0) {
        pane0.getStyleClass().add("login-form");

        dom.vbox(pane1 -> {
            pane1.getStyleClass().add("login-body");

            dom.vbox(pane2 -> {
                pane2.getStyleClass().add("login-logo");

                dom.img(img -> {
                    img.setImage(ResourceCatalog.getImage("images/big_logo.png"));
                });
            });

            dom.label(label -> {
                label.getStyleClass().add("login-lbl-usr");

                label.setText("Usuário");
            });

            dom.textField(field -> {
                field.getStyleClass().add("login-fld-usr");
                this.usr = field;
            });

            dom.label(label -> {
                label.getStyleClass().add("login-lbl-pwd");
                label.setText("Senha");
            });

            dom.passwordField(field -> {
                field.getStyleClass().add("login-fld-pwd");
                field.setOnKeyPressed(this::emitOnPwdKeyDown);
                this.pwd = field;
            });

            dom.vbox(pane2 -> {
                pane2.setAlignment(Pos.CENTER_RIGHT);

                dom.button(button0 -> {
                    button0.getStyleClass().add("login-button");

                    button0.setText("LOGIN");
                    button0.setDefaultButton(true);

                    button0.setOnAction(this::emitLogin);
                });

            });

            dom.label(label -> {
                label.getStyleClass().add("login-error");
                label.setVisible(this.state.errorCode != 0);
                label.setText(this.state.errorMessage);
                this.errorDiv = label;
            });
        });
    }

    private void populateState() {
        this.state.userName = this.usr.getText();
        this.state.password = this.pwd.getText();
    }

    private Object emitOnPwdKeyDown(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            populateState();
            this.presenter.onEnter().finally_(this::update);
        }

        return null;
    }

    public void emitLogin(ActionEvent evt) {
        populateState();
        this.presenter.onEnter().finally_(this::update);
    }

}
