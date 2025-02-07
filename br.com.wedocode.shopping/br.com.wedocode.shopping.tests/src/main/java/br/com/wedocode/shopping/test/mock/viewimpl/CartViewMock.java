package br.com.wedocode.shopping.test.mock.viewimpl;

import org.junit.Assert;

import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class CartViewMock extends AbstractViewMock<CartPresenter> {

    public static CartViewMock cast(WebFlowView view) {
        var cls = CartViewMock.class;
        Assert.assertNotNull("Expecting " + cls.getSimpleName() + " but this view was null", view);
        Assert.assertTrue("Expecting " + cls.getSimpleName() + " but it was " + view.getClass().getSimpleName(),
                cls.isInstance(view));
        return (CartViewMock) view;
    }

    public CartPresenter.CartState state;

    public CartViewMock(ShoppingApplicationMock app, CartPresenter presenter) {
        super(app, presenter);
        this.state = presenter.getState();
    }

    public void printCarrinho_() {
        for (final CartItem item : this.state.items) {
            System.out.print("{id: ");
            System.out.print(item.id);
            System.out.print(", nome: ");
            System.out.print(item.name);
            System.out.print(", preco: ");
            System.out.print(item.price);
            System.out.print(", imagem: ");
            System.out.print(item.image);
            System.out.println("}");
        }
        System.out.println();
    }

}
