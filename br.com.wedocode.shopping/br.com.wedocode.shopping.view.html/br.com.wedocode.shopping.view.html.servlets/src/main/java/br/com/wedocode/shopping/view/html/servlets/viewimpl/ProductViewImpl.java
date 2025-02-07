package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.io.PrintWriter;
import java.text.NumberFormat;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.view.html.servlets.util.EventIdGenerator;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public class ProductViewImpl extends AbstractView {

    /*
     * Events
     */

    private static final String ON_ADD_TO_CART = EventIdGenerator.nextAsString();

    private static final String ON_OPEN_PRODUCTS = EventIdGenerator.nextAsString();

    /*
     * Fields
     */

    private final NumberFormat numFormater;

    private ProductPresenter presenter;

    private ProductPresenter.ProductState state;

    /*
     * Constructor
     */

    public ProductViewImpl(ShoppingApplicationImpl app, ProductPresenter presenter) {
        this.presenter = presenter;
        this.numFormater = app.formatNumber();
        this.state = presenter.getState();
    }

    /*
     * API
     */

    @Override
    public void syncState(FormData form, Promise<Boolean> action) {
        if (ON_ADD_TO_CART.equals(form.getEventId())) {
            var quantidade = form.getInteger("quantidade");
            action.then(this.presenter.onAddToCart(quantidade));
            return;
        }

        if (ON_OPEN_PRODUCTS.equals(form.getEventId())) {
            action.then(this.presenter.onOpenProducts());
            return;
        }
    }

    @Override
    public void render(PrintWriter wr) {

        final String onAddToCartEvent = "app.submit(" + ON_ADD_TO_CART + ")";
        final String onOpenProductsEvent = "app.submit(" + ON_OPEN_PRODUCTS + ")";

        final String htmlNome = escapeHtml4(this.state.product.name);
        final String htmlDescription = this.state.product.description; // Is an valid HTML

        wr.println("<div id=\"boxProdutos\">");
        wr.println("	<div id=\"breadcrumb\">");
        wr.println("		<p>");
        wr.println("			<b>Produtos &gt; " + htmlNome + "</b>");
        wr.println("		</p>");
        wr.println("	</div>");

        wr.println("	<div id=\"fotoproduto\">");
        wr.println("		<img id=\"logo\" src=\"" + this.state.product.image + "\" />");
        wr.println("	</div>");

        wr.println("	<div id=\"gdescricao\">");

        wr.println("		<div id=\"tituloProduto\">");
        wr.println("			<h1>" + htmlNome + "</h1>");
        wr.println("			<h2>R$ " + this.numFormater.format(this.state.product.price) + "</h2>");

        wr.println(
                "			<div id=\"quantidade\">Quantidade: <input type=\"text\" name=\"quantidade\" value=\"1\"/></div>");

        wr.println("			<div id=\"descricao\" style=\"padding-top: 30px;\">");
        wr.println("				<h1>DESCRIÇÃO DO PRODUTO</h1>");
        wr.println("				<div>" + htmlDescription + "</div>");
        wr.println("				<div id=\"comprar\">");
        wr.println("					<button onclick=\"" + onAddToCartEvent + "\">");
        wr.println("						<a class=\"link\">COMPRAR</a>");
        wr.println("					</button>");
        wr.println("				</div>");
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

        wr.println("	<div id=\"naveg\">");
        wr.println("		<button onclick=\"" + onOpenProductsEvent + "\">");
        wr.println("			<a class=\"link\">&lt; VOLTAR</a>");
        wr.println("		</button>");
        wr.println("	</div>");
        wr.println("</div>");
    }

}
