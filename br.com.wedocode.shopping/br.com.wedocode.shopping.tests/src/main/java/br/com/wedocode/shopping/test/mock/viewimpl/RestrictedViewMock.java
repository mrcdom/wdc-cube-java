package br.com.wedocode.shopping.test.mock.viewimpl;

import java.util.Date;

import org.junit.Assert;

import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class RestrictedViewMock extends AbstractViewMock<RestrictedPresenter> {

    public static RestrictedViewMock cast(WebFlowView view) {
        var cls = RestrictedViewMock.class;
        Assert.assertNotNull("Expecting " + cls.getSimpleName() + " but this view was null", view);
        Assert.assertTrue("Expecting " + cls.getSimpleName() + " but it was " + view.getClass().getSimpleName(),
                cls.isInstance(view));
        return (RestrictedViewMock) view;
    }

    public RestrictedPresenter.RestrictedState state;

    public RestrictedViewMock(ShoppingApplicationMock app, RestrictedPresenter presenter) {
        super(app, presenter);
        this.state = presenter.getState();
    }

    public void render() {
        System.out.println("Seja bem vindo, " + this.state.nickName + "!");

        System.out.println();

        System.out.println("Carrinho[" + this.state.cartItemCount + "]");
        System.out.println();

        this.printCompras();
        this.printProdutos();

        System.out.println("---------------------------------------------------------");
    }

    public void printCompras() {
        for (final PurchaseInfo compra : this.state.purchases) {
            System.out.println("COMPRA #" + compra.id);
            System.out.println("Data da compra: " + new Date(compra.date));
            System.out.println("Itens adquiridos: " + compra.items);
            System.out.println("Valor total: R$ " + compra.total);
            System.out.println();
        }

    }

    public void printProdutos() {
        for (final ProductItem produto : this.state.products) {
            System.out.print("PRODUTO #" + produto.id);
            System.out.print("{nome: ");
            System.out.print(produto.name);
            System.out.print(", valor: ");
            System.out.print(produto.price);
            System.out.print(", imagem: ");
            System.out.print(produto.image);
            System.out.println("}");
        }
    }

}
