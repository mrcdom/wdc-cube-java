package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.io.PrintWriter;
import java.text.NumberFormat;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptItem;
import br.com.wedocode.shopping.view.html.servlets.util.EventIdGenerator;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public class ReceiptViewImpl extends AbstractView {

    /*
     * Events
     */

    private static final String ON_OPEN_PRODUCTS = EventIdGenerator.nextAsString();

    /*
     * Fields
     */

    private ReceiptPresenter presenter;

    private ReceiptPresenter.ReceiptState state;

    private final NumberFormat numFormater;

    /*
     * Constructor
     */

    public ReceiptViewImpl(ShoppingApplicationImpl app, ReceiptPresenter presenter) {
        this.presenter = presenter;
        this.state = presenter.getState();
        this.numFormater = app.formatNumber();
    }

    @Override
    public void syncState(FormData form, Promise<Boolean> action) {
        if (ON_OPEN_PRODUCTS.equals(form.getEventId())) {
            action.then(this.presenter.onOpenProducts());
            return;
        }
    }

    @Override
    public void render(PrintWriter wr) {
        var onOpenProductsEvent = "app.submit(" + ON_OPEN_PRODUCTS + ")";

        wr.println("<div id=\"boxProdutos\">");

        if (this.state.notifySuccess) {
            wr.println("<h1>COMPRA EFETUADA COM SUCESSO</h1>");
            this.state.notifySuccess = false;
        }

        wr.println("<h2>IMPRIMA SEU RECIBO:</h2>");

        wr.println("	<div id=\"recibo\">");
        wr.println("		<div id=\"reciboTopo\">");
        wr.println("			<h3>WEDOCODE SHOPPING - SUA COMPRA CERTA NA INTERNET</h3>");
        wr.println("			<h3>Recibo de compra</h3>");
        wr.println("		</div>");
        wr.println("		<div id=\"cel1\">ITEM</div>");
        wr.println("		<div id=\"cel1\">VALOR</div>");
        wr.println("		<div id=\"cel1\">QUANTIDADE</div>");
        for (final ReceiptItem item : this.state.receipt.items) {
            var htmlDescricao = escapeHtml4(item.description);

            wr.println("		<div id=\"borda\"></div>");
            wr.println("		<div id=\"cel1\">" + htmlDescricao + "</div>");
            wr.println("		<div id=\"cel1\">R$ " + this.numFormater.format(item.value) + "</div>");
            wr.println("		<div id=\"cel1\">" + item.quantity + "</div>");
        }
        wr.println("		<div id=\"borda2\"></div>");
        wr.println("		<div id=\"borda\"></div>");
        wr.println("		<div id=\"valorcompra\">VALOR TOTAL: R$ "
                + this.numFormater.format(this.state.receipt.total) + "</div>");

        wr.println("	</div>");

        wr.println("	<div id=\"naveg\">");
        wr.println("		<button onclick=\"" + onOpenProductsEvent + "\">");
        wr.println("			<a class=\"link\">&lt; VOLTAR</a>");
        wr.println("		</button>");
        wr.println("	</div>");

        wr.println("</div>");
    }

}
