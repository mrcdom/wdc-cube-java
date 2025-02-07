package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import java.io.PrintWriter;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.view.html.servlets.util.EventIdGenerator;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public class CartViewImpl extends AbstractView {

    /*
     * Events
     */

    private static final String ON_BUY = EventIdGenerator.nextAsString();

    private static final String ON_REMOVE_PRODUCT = EventIdGenerator.nextAsString();

    private static final String ON_OPEN_PRODUCTS = EventIdGenerator.nextAsString();

    /*
     * Fields
     */

    private final NumberFormat numFormater;

    private CartPresenter presenter;

    private CartPresenter.CartState state;

    public CartViewImpl(ShoppingApplicationImpl app, CartPresenter presenter) {
        this.presenter = presenter;
        this.numFormater = app.formatNumber();
        this.state = presenter.getState();
    }

    @Override
    public void syncState(FormData form, Promise<Boolean> action) {
        if (ON_REMOVE_PRODUCT.equals(form.getEventId())) {
            var productId = form.getLong("productId");
            action.then(this.presenter.onRemoveProduct(productId));
            return;
        }

        if (ON_BUY.equals(form.getEventId())) {
            action.then(this.presenter.onBuy());
            return;
        }

        if (ON_OPEN_PRODUCTS.equals(form.getEventId())) {
            action.then(this.presenter.onOpenProducts());
            return;
        }
    }

    @Override
    public void render(PrintWriter wr) {
        final String finalizarClick = "app.submit(" + ON_BUY + ")";
        final String voltarClick = "app.submit(" + ON_OPEN_PRODUCTS + ")";

        wr.println("<div id=\"boxProdutos\">");
        wr.println("	<div id=\"carQtcarro\">");
        wr.println("		<div id=\"btnCarrinho\">");
        wr.println("			<img src=\"images/carrinho.png\" />");
        wr.println("			<h5>Carrinho</h5>");
        wr.println("			<h6>[" + this.state.items.size() + "]</h6>");
        wr.println("		</div>");
        wr.println("		<h2>LISTA DE PRODUTOS</h2>");
        wr.println("	</div>");

        wr.println("	<div id=\"carrecibo\">");
        wr.println("		<div id=\"carreciboTopo\"></div>");

        wr.println("		<div id=\"carcel1\">ITEM</div>");
        wr.println("		<div id=\"carcel1\">VALOR</div>");
        wr.println("		<div id=\"carcel1\">QUANTIDADE</div>");

        double total = 0;
        for (final CartItem item : this.state.items) {
            final String htmlNome = StringEscapeUtils.escapeHtml4(item.name);
            final String removerClick = "app.submit(" + ON_REMOVE_PRODUCT + ", {productId: " + item.id + "})";
            final String htmlQuantidade = StringUtils.leftPad(String.valueOf(item.quantity), 2, '0');

            wr.println("		<div id=\"carcel2\">");
            wr.println("			<img class=\"mini-img-produto\" src=\"" + item.image + "\" />" + htmlNome);
            wr.println("		</div>");
            wr.println("		<div id=\"carcel2\">R$ " + this.numFormater.format(item.price) + "</div>");
            wr.println("		<div id=\"carcel2b\">");
            wr.println("			" + htmlQuantidade + "<a><img src=\"images/delet.png\" onclick=\"" + removerClick
                    + "\"></a>");
            wr.println("		</div>");

            total += item.price * item.quantity;
        }

        wr.println("		<div id=\"carvalorcompra\">VALOR TOTAL: R$ " + this.numFormater.format(total) + "</div>");
        wr.println("	</div>");

        if (this.state.errorCode != 0) {
            wr.print("	<div id=\"error\">");
            wr.print(StringEscapeUtils.escapeHtml4(this.state.errorMessage));
            wr.println("	</div>");
            this.state.errorCode = 0;
            this.state.errorMessage = null;
        }

        if (!this.state.items.isEmpty()) {
            wr.println("	<div id=\"finalizacompra\">");
            wr.println("		<button onclick=\"" + finalizarClick + "\">");
            wr.println("			<a class=\"link\">FINALIZAR PEDIDO</a>");
            wr.println("		</button>");
            wr.println("	</div>");
        }

        wr.println("	<div id=\"naveg\">");
        wr.println("		<button onclick=\"" + voltarClick + "\">");
        wr.println("			<a class=\"link\">&lt; VOLTAR</a>");
        wr.println("		</button>");
        wr.println("	</div>");

        wr.println("</div>");
    }

}
