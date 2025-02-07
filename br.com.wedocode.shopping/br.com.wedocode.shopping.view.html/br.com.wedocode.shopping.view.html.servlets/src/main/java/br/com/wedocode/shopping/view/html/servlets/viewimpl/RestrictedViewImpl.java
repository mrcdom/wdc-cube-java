package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.StringEscapeUtils;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter.RestrictedState;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.view.html.servlets.util.EventIdGenerator;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public class RestrictedViewImpl extends AbstractView {

    /*
     * Events
     */

    private static final String ON_EXIT = EventIdGenerator.nextAsString();

    private static final String ON_OPEN_CART = EventIdGenerator.nextAsString();

    private static final String ON_OPEN_RECEIPT = EventIdGenerator.nextAsString();

    private static final String ON_OPEN_PRODUCT = EventIdGenerator.nextAsString();

    /*
     * Fields
     */

    private final NumberFormat numFormater;

    private RestrictedPresenter presenter;

    private RestrictedState state;

    public RestrictedViewImpl(ShoppingApplicationImpl app, RestrictedPresenter presenter) {
        this.presenter = presenter;
        this.numFormater = app.formatNumber();
        this.state = presenter.getState();
    }

    @Override
    public void syncState(FormData form, Promise<Boolean> action) {
        if (ON_EXIT.equals(form.getEventId())) {
            action.then(this.presenter.onExit());
            return;
        }

        if (ON_OPEN_CART.equals(form.getEventId())) {
            action.then(this.presenter.onOpenCart());
            return;
        }

        if (ON_OPEN_RECEIPT.equals(form.getEventId())) {
            final Long idCompra = form.getLong("idCompra");
            action.then(this.presenter.onOpenReceipt(idCompra));
            return;
        }

        if (ON_OPEN_PRODUCT.equals(form.getEventId())) {
            final Long idProduto = form.getLong("idProduto");
            action.then(this.presenter.onOpenProduct(idProduto));
            return;
        }

        this.syncStateChild(this.state.contentView, form, action);
    }

    @Override
    public void render(PrintWriter wr) {

        final String onOpenCartEvent = "app.submit(" + ON_OPEN_CART + ")";
        final String onExit = "app.submit(" + ON_EXIT + ")";

        wr.println("<div id=\"header\">");
        wr.println("	<div class=\"left\">");
        wr.println("		<img id=\"logo\" src=\"images/logo.png\" alt=\"WeDoCode Shopping\" />");
        wr.println("	</div>");
        wr.println("	<div class=\"right\">");
        wr.println("		<div id=\"btnCarrinho\">");
        wr.println("			<a href=\"javascript:void(" + onOpenCartEvent + ")\">");
        wr.println("				<img src=\"images/carrinho.png\" alt=\"WeDoCode Shopping\" />");
        wr.println("				<h5>Carrinho</h5>");
        wr.println("				<h6>[" + this.state.cartItemCount + "]</h6>");
        wr.println("			</a>");
        wr.println("		</div>");
        wr.println("	</div>");
        wr.println("</div>");

        wr.println("<div class=\"center\">");
        wr.println("	<div id=\"grandeC\">");
        wr.println("		<div id=\"boasvindas\">");
        wr.println("			<p>");
        wr.println("				Seja bem vindo, <b>" + escapeHtml4(this.state.nickName) + "!</b>");
        wr.println("				<div id=\"sair2\">");
        wr.println("					<button onclick=\"" + onExit + "\"><a class=\"link\">sair</a></button>");
        wr.println("				</div>");
        wr.println("			</p>");

        if (!this.renderChild(this.state.contentView, wr)) {
            this.renderProducts(wr);
        }

        wr.println("		</div>");
        wr.println("	</div>");
        wr.println("</div>");
    }

    void renderProducts(PrintWriter wr) {
        if (this.state.purchases != null && this.state.purchases.size() > 0) {
            wr.println("<div id=\"menu\">");

            wr.println("	<h4>Seu histórico de compras</h4>");
            for (final PurchaseInfo compra : this.state.purchases) {
                final String compraClick = "app.submit(" + ON_OPEN_RECEIPT + ",{idCompra: " + compra.id + "})";

                String sdate;
                {
                    var date = LocalDate.ofEpochDay(TimeUnit.DAYS.convert(compra.date, TimeUnit.MILLISECONDS));

                    var y = String.valueOf(date.getYear());
                    var m = date.getMonthValue() < 10 ? "0" + date.getMonthValue()
                            : String.valueOf(date.getMonthValue());
                    var d = date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth()
                            : String.valueOf(date.getDayOfMonth());

                    sdate = d + '/' + m + '/' + y;
                }
                final String svalor = StringEscapeUtils.escapeHtml4(this.numFormater.format(compra.total));

                wr.println("	<div id=\"hbox\">");
                wr.println("	<h1>Compra #" + compra.id + "</h1>");
                wr.println("	<h2>Data da compra:</h2>");
                wr.println("	<p>" + sdate + "</p>");
                wr.println("	<h2>Itens adquiridos:</h2>");
                wr.println("	<p>");
                for (int i = 0, iLast = compra.items.size() - 1; i <= iLast; i++) {
                    final String iten = compra.items.get(i);
                    wr.println(StringEscapeUtils.escapeHtml4(iten));
                    if (i < iLast) {
                        wr.println(", ");
                    }
                }
                wr.println("	</p>");

                wr.println("	<p><b>Valor Total: </b>R$ " + svalor + "</p>");
                wr.println("	<div id=\"vermais\">");
                wr.println("		<button onclick=\"" + compraClick + "\">");
                wr.println("			<a class=\"link\">VEJA MAIS DETALHES</a>");
                wr.println("		</button></div>");

                wr.println("	</div>");
            }
            // fim div menu
            wr.println("</div>");

            // beging produto
            wr.println("<div id=\"contprod\">");
        } else {
            // begin produto
            wr.println("<div id=\"boxProdutos\">");
        }

        wr.println("<h1>PRODUTOS</h1>");

        if (this.state.products != null && this.state.products.size() > 0) {
            for (final ProductItem produto : this.state.products) {
                final String svalor = this.numFormater.format(produto.price);
                final String sname = escapeHtml4(produto.name);
                final String productClick = "app.submit(" + ON_OPEN_PRODUCT + ",{idProduto: " + produto.id + "})";

                wr.println("<div id=\"miniBox\" class=\"product-box\" onclick=\"" + productClick + "\">");
                wr.println("	<div id=\"afoto\">");
                wr.println("		<img src=\"" + produto.image + "\" style=\"width: 194px; height: 152px;\">");
                wr.println("	</div>");
                wr.println("	<div id=\"bfoto\">");
                wr.println("		<a class=\"link\">" + sname + "<br><b>R$ " + svalor + "</b></a>");
                wr.println("	</div>");
                wr.println("</div>");
            }
        }

        // fim produto
        wr.println("</div>");
    }

}
