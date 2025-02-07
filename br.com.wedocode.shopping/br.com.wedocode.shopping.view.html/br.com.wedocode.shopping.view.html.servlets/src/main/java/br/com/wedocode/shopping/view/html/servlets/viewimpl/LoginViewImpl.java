package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.io.PrintWriter;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.view.html.servlets.util.EventIdGenerator;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public class LoginViewImpl extends AbstractView {

    /*
     * Events
     */

    private static final String ON_ENTER = EventIdGenerator.nextAsString();

    /*
     * Fields
     */

    private LoginPresenter presenter;

    private LoginPresenter.LoginState state;

    public LoginViewImpl(ShoppingApplicationImpl app, LoginPresenter presenter) {
        this.presenter = presenter;
        this.state = presenter.getState();
    }

    @Override
    public void syncState(FormData form, Promise<Boolean> action) {
        if (ON_ENTER.equals(form.getEventId())) {
            this.state.userName = form.getString("usr");
            this.state.password = form.getString("pwd");
            action.then(this.presenter.onEnter());
            return;
        }
    }

    @Override
    public void render(PrintWriter wr) {
        var loginEvent = "app.submit(" + ON_ENTER + ")";
        wr.println("<div class=\"center\">");
        wr.println("	<div id=\"formulario\">");
        wr.println("		<img id=\"logo\" src=\"images/big_logo.png\" alt=\"WeDoCode Shopping\" />");
        wr.println("		<div id=\"campos\">");
        wr.println("			<p>Usuário</p>");
        wr.println("			<input type=\"text\" name=\"usr\" />");
        wr.println("			<p>Senha</p>");
        wr.println("			<input type=\"password\" name=\"pwd\" onkeypress=\"app.submitOnEnterPressed(event, "
                + ON_ENTER + ")\"/>");
        wr.println("			<div id=\"login\">");
        wr.println("				<button onclick=\"" + loginEvent
                + "\"><a class=\"link\">&nbsp;LOGIN&nbsp;</a></button>");
        wr.println("			</div>");

        if (this.state.errorCode != 0) {
            wr.println("<div class=\"error\">");
            wr.println(escapeHtml4(this.state.errorMessage));
            wr.println("</div>");
            this.state.errorCode = 0;
            this.state.errorMessage = null;
        }

        wr.println("		</div>");
        wr.println("	</div>");
        wr.println("</div>");
    }

}
