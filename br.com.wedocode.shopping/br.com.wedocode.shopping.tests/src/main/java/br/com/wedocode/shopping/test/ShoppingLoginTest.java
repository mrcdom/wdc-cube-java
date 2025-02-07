package br.com.wedocode.shopping.test;

import org.junit.Assert;
import org.junit.Test;

import br.com.wedocode.framework.commons.util.StringUtils;
import br.com.wedocode.shopping.business.jdbc.util.DBReset;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.test.mock.viewimpl.LoginViewMock;
import br.com.wedocode.shopping.test.mock.viewimpl.RestrictedViewMock;
import br.com.wedocode.shopping.test.util.BasePresentationTest;

public class ShoppingLoginTest extends BasePresentationTest {

    @Test
    public void testLoginPrimeiroAcesso() throws Exception {
        wait(Routes.login(this.app));

        var rootView = this.app.getRootView();

        var mainContent = LoginViewMock.cast(rootView.state.contentView);
        Assert.assertTrue("Usuário não poderia ter sido validado", mainContent.state.errorCode == 0);
    }

    @Test
    public void testLoginFalhaPorSenhaOuUsuarioNaoReconhecidos() throws Exception {
        wait(Routes.login(this.app));

        var rootView = this.app.getRootView();

        var loginView = LoginViewMock.cast(rootView.state.contentView);
        loginView.state.userName = "admin";
        loginView.state.password = "senha não reconhecida";
        wait(loginView.presenter.onEnter());

        // Check if it keeps bean login view
        loginView = LoginViewMock.cast(rootView.state.contentView);
        Assert.assertTrue("Usuário não poderia ter sido validado", loginView.state.errorCode == 1);
    }

    @Test
    public void testLoginAcessoAoSistema() throws Exception {
        wait(Routes.login(this.app));

        var rootView = this.app.getRootView();

        var loginView = LoginViewMock.cast(rootView.state.contentView);
        loginView.state.userName = "admin";
        loginView.state.password = "admin";
        wait(loginView.presenter.onEnter());

        var restrictedView = RestrictedViewMock.cast(rootView.state.contentView);

        Assert.assertTrue("Nome do usuário inválido", StringUtils.isNotBlank(restrictedView.state.nickName));
        Assert.assertNotNull("Falta lista de compras", restrictedView.state.purchases);
        Assert.assertNotNull("Falta lista de produtos", restrictedView.state.products);
        Assert.assertTrue("Quantidade itens no carrinho não pode ser negativo",
                restrictedView.state.cartItemCount >= 0);
        Assert.assertTrue("Usuário deveria ter sido validado", restrictedView.state.errorCode == 0);

        Assert.assertEquals("João da Silva", restrictedView.state.nickName);
        Assert.assertEquals(0, restrictedView.state.cartItemCount);
        Assert.assertEquals(2, restrictedView.state.purchases.size());

        Assert.assertEquals(DBReset.ADMIN_FIRST_PURCHASE_ID, restrictedView.state.purchases.get(0).id);
        Assert.assertEquals(200.0, restrictedView.state.purchases.get(0).total, 0.001);
        Assert.assertEquals(1, restrictedView.state.purchases.get(0).items.size());
        Assert.assertEquals("Cafeteira design italiano", restrictedView.state.purchases.get(0).items.get(0));

        Assert.assertEquals(DBReset.ADMIN_SECOND_PURCHASE_ID, restrictedView.state.purchases.get(1).id);
        Assert.assertEquals(47.97, restrictedView.state.purchases.get(1).total, 0.001);
        Assert.assertEquals(2, restrictedView.state.purchases.get(1).items.size());
        Assert.assertEquals("Bola Wilson", restrictedView.state.purchases.get(1).items.get(0));
        Assert.assertEquals("Fita veda rosca", restrictedView.state.purchases.get(1).items.get(1));

        Assert.assertEquals(4, restrictedView.state.products.size());

        Assert.assertEquals(DBReset.CAFETEIRA_ID, restrictedView.state.products.get(0).id);
        Assert.assertNull(restrictedView.state.products.get(0).description);

        Assert.assertEquals(DBReset.BOLA_WILSON_ID, restrictedView.state.products.get(1).id);
        Assert.assertNull(restrictedView.state.products.get(1).description);

        Assert.assertEquals(DBReset.FITA_VEDA_ROSCA_ID, restrictedView.state.products.get(2).id);
        Assert.assertNull(restrictedView.state.products.get(2).description);

        Assert.assertEquals(DBReset.PEN_DRIVE2GB_ID, restrictedView.state.products.get(3).id);
        Assert.assertNull(restrictedView.state.products.get(3).description);
    }

}
