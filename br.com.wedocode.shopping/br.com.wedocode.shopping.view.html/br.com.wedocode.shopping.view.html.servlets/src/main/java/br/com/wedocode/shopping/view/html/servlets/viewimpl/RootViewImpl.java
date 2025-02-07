package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.RootPresenter;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public class RootViewImpl extends AbstractView {

    /*
     * Fields
     */

    private final ShoppingApplicationImpl app;

    private RootPresenter.RootState state;

    /*
     * Constructors
     */

    public RootViewImpl(ShoppingApplicationImpl app, RootPresenter presenter) {
        this.app = app;
        this.state = presenter.getState();
    }

    public WebFlowView getContentView() {
        return this.state.contentView;
    }

    @Override
    public void syncState(FormData form, Promise<Boolean> actions) {
        this.syncStateChild(this.state.contentView, form, actions);
    }

    @Override
    public void render(PrintWriter wr) {
        if (this.state.errorMessage != null) {
            wr.println("<DIV class=\"error\">");
            wr.println("error: " + this.state.errorMessage + ")");
            wr.println("</DIV>");
            this.state.errorMessage = null;
        } else {
            wr.println("<form id=\"main-form\" action=\"do\" method=\"POST\" onsubmit=\"return false;\">");

            final var place = this.app.getFragment();
            if (StringUtils.isNotBlank(place)) {
                wr.println(
                        "<input type=\"hidden\" id=\"place\" name=\"place\" value=\"" + escapeHtml4(place) + "\" />");
            }

            this.renderChild(this.state.contentView, wr);

            wr.println("</form>");
        }
    }

}
