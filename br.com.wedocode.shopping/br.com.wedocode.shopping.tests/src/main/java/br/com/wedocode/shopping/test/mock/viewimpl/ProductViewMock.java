package br.com.wedocode.shopping.test.mock.viewimpl;

import org.junit.Assert;

import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class ProductViewMock extends AbstractViewMock<ProductPresenter> {

    public static ProductViewMock cast(WebFlowView view) {
        var cls = ProductViewMock.class;
        Assert.assertNotNull("Expecting " + cls.getSimpleName() + " but this view was null", view);
        Assert.assertTrue("Expecting " + cls.getSimpleName() + " but it was " + view.getClass().getSimpleName(),
                cls.isInstance(view));
        return (ProductViewMock) view;
    }

    public ProductPresenter.ProductState state;

    public ProductViewMock(ShoppingApplicationMock app, ProductPresenter presenter) {
        super(app, presenter);
        this.state = presenter.getState();
    }

    public void printProduto_() {
        System.out.println("PRODUTO #" + this.state.product.id);
        System.out.println("Nome: " + this.state.product.name);
        System.out.println("Preço: " + this.state.product.price);
        System.out.println("Descrição: " + this.state.product.description);
        System.out.println("Imagem: " + this.state.product.image);
        System.out.println();
    }

}
