package br.com.wedocode.shopping.test;

import org.junit.Assert;
import org.junit.Test;

import br.com.wedocode.shopping.business.jdbc.util.DBReset;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.test.mock.viewimpl.CartViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.LoginViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.ProductViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.ReceiptViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.RestrictedViewMock;
import br.com.wedocode.shopping.test.util.BasePresentationTest;

public class ShoppingWorkflowTest extends BasePresentationTest {

    private RestrictedViewMock gotoRestricted() throws Exception {
        wait(Routes.login(this.app));

        var rootView = this.app.getRootView();

        var loginView = LoginViewMock.cast(rootView.state.contentView);
        loginView.state.userName = "admin";
        loginView.state.password = "admin";
        wait(loginView.presenter.onEnter());

        return RestrictedViewMock.cast(rootView.state.contentView);
    }

    @Test
    public void testVisualizaProdutoInexistente() throws Exception {
        var restrictedView = gotoRestricted();
        var rootView = this.app.getRootView();

        // Produto que não existe
        wait(restrictedView.presenter.onOpenProduct(Long.MIN_VALUE));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        Assert.assertTrue("O código de erro deve estar indicado produto não existe: " + restrictedView.state.errorCode,
                restrictedView.state.errorCode == 3);
    }

    @Test
    public void testVisualizaProduto() throws Exception {
        var restrictedView = gotoRestricted();
        var rootView = this.app.getRootView();

        wait(restrictedView.presenter.onOpenProduct(DBReset.PEN_DRIVE2GB_ID));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);

        var produtoView = ProductViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Produto deve ter sido selecionado", produtoView.state.product != null);
    }

    @Test
    public void testComprarProduto() throws Exception {
        wait(Routes.login(this.app));

        var rootView = this.app.getRootView();

        var loginView = LoginViewMock.cast(rootView.state.contentView);
        loginView.state.userName = "admin";
        loginView.state.password = "admin";
        wait(loginView.presenter.onEnter());

        var restrictedView = RestrictedViewMock.cast(rootView.state.contentView);

        wait(restrictedView.presenter.onOpenProduct(DBReset.PEN_DRIVE2GB_ID));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        var produtoView = ProductViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Produto deve ter sido selecionado", produtoView.state.product != null);
        Assert.assertTrue("Produto deve ser o id==" + DBReset.PEN_DRIVE2GB_ID,
                produtoView.state.product.id == DBReset.PEN_DRIVE2GB_ID);

        wait(produtoView.presenter.onAddToCart(1));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        var carrinhoView = CartViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Não deve haver indicação de erros", carrinhoView.state.errorCode == 0);
        Assert.assertTrue("Um item no carrinho", carrinhoView.state.items.size() == 1);
        Assert.assertTrue("O item deve ter quantidade 1", carrinhoView.state.items.get(0).quantity == 1);
        Assert.assertTrue("ID no carriho <> " + DBReset.PEN_DRIVE2GB_ID,
                carrinhoView.state.items.get(0).id == DBReset.PEN_DRIVE2GB_ID);

        wait(carrinhoView.presenter.onModifyQuantity(DBReset.PEN_DRIVE2GB_ID, 0));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        carrinhoView = CartViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Indicação de quantidade inválida", carrinhoView.state.errorCode == 1);
        carrinhoView.state.errorCode = 0;

        wait(carrinhoView.presenter.onModifyQuantity(DBReset.PEN_DRIVE2GB_ID, 2));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        carrinhoView = CartViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Tem que funcionar sem erro", carrinhoView.state.errorCode == 0);
        Assert.assertTrue("O item deve ter quantidade 2", carrinhoView.state.items.get(0).quantity == 2);

        wait(carrinhoView.presenter.onModifyQuantity(Long.MIN_VALUE, 2));
        carrinhoView = CartViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Produto não encontrado", carrinhoView.state.errorCode == 2);
        carrinhoView.state.errorCode = 0;

        wait(carrinhoView.presenter.onOpenProducts());
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);

        wait(restrictedView.presenter.onOpenProduct(DBReset.BOLA_WILSON_ID));
        produtoView = ProductViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Produto BOLA_WILSON não localizado", produtoView.state.product.id == DBReset.BOLA_WILSON_ID);

        wait(produtoView.presenter.onOpenProducts());
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);

        wait(restrictedView.presenter.onOpenProduct(DBReset.FITA_VEDA_ROSCA_ID));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        produtoView = ProductViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Produto FITA_VEDA_ROSCA não localizado",
                produtoView.state.product.id == DBReset.FITA_VEDA_ROSCA_ID);

        wait(produtoView.presenter.onAddToCart(1));
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        carrinhoView = CartViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Um item no carrinho", carrinhoView.state.items.size() == 2);
        Assert.assertTrue("O item deve ter quantidade 1", carrinhoView.state.items.get(1).quantity == 1);

        wait(carrinhoView.presenter.onBuy());
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        var reciboView = ReceiptViewMock.cast(restrictedView.state.contentView);
        Assert.assertTrue("Tem que estar marcado como novo recibo", reciboView.state.notifySuccess);
        Assert.assertTrue(reciboView.state.receipt != null);
        Assert.assertTrue(reciboView.state.receipt.items.size() == 2);

        wait(reciboView.presenter.onOpenProducts());
        restrictedView = RestrictedViewMock.cast(rootView.state.contentView);
        Assert.assertNull("A visão restrita deveria estar mostrando o conteúdo padrão",
                restrictedView.state.contentView);
    }

}
