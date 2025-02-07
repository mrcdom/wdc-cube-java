package br.com.wedocode.shopping.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.wedocode.framework.commons.util.StringUtils;
import br.com.wedocode.shopping.business.jdbc.util.DBReset;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;
import br.com.wedocode.shopping.test.util.BaseBusinessTest;

public class ShoppingDAOTest extends BaseBusinessTest {

    @Test
    public void test() throws Exception {
        var subject = wait(this.dao.loadSubject("admin", "admin"));
        Assert.assertNotNull("Missing subject", subject);

        Assert.assertTrue("Subject.id must be a Long type", subject.getId() instanceof Long);
        var userId = subject.getId();

        Assert.assertEquals("UserId must be administrator", DBReset.ADMIN_ID, userId.longValue());

        Assert.assertEquals("User name did not match", "João da Silva", subject.getNickName());

        var produtos = wait(this.dao.loadProducts(true));
        Assert.assertNotNull(produtos);
        Assert.assertEquals(4, produtos.size());
        Assert.assertEquals(DBReset.CAFETEIRA_ID, produtos.get(0).id);
        Assert.assertEquals(DBReset.BOLA_WILSON_ID, produtos.get(1).id);
        Assert.assertEquals(DBReset.FITA_VEDA_ROSCA_ID, produtos.get(2).id);
        Assert.assertEquals(DBReset.PEN_DRIVE2GB_ID, produtos.get(3).id);

        for (final ProductItem produto : produtos) {
            Assert.assertTrue("Product name can not be empty", StringUtils.isNotBlank(produto.name));
            Assert.assertTrue("Product image name can not end differently than .png", produto.image.endsWith(".png"));
            Assert.assertTrue("Product price must be grater than or equal to 0.0", produto.price >= 0.0);
            Assert.assertTrue("Product description can not be empty", StringUtils.isNotBlank(produto.description));

            final ProductItem mesmoProduto = wait(this.dao.loadProductById(produto.id));
            Assert.assertEquals(produto.id, mesmoProduto.id);
            Assert.assertEquals(produto.name, mesmoProduto.name);
            Assert.assertEquals(produto.image, mesmoProduto.image);
            Assert.assertEquals(produto.price, mesmoProduto.price, 0.001);
            Assert.assertEquals(produto.description, mesmoProduto.description);
        }

        List<PurchaseInfo> compras = wait(this.dao.loadPurchases(userId));
        Assert.assertNotNull(compras);
        Assert.assertEquals(2, compras.size());

        Assert.assertEquals(DBReset.ADMIN_FIRST_PURCHASE_ID, compras.get(0).id);
        Assert.assertNotNull(compras.get(0).items);
        Assert.assertEquals(1, compras.get(0).items.size());

        Assert.assertEquals(DBReset.ADMIN_SECOND_PURCHASE_ID, compras.get(1).id);
        Assert.assertNotNull(compras.get(1).items);
        Assert.assertEquals(2, compras.get(1).items.size());

        PurchaseItem pedido0, pedido1;
        final List<PurchaseItem> carrinho = new ArrayList<>();
        {
            pedido0 = new PurchaseItem();
            pedido0.productId = DBReset.PEN_DRIVE2GB_ID;
            pedido0.price = 50;
            pedido0.quantity = 1;
            carrinho.add(pedido0);

            pedido1 = new PurchaseItem();
            pedido1.productId = DBReset.FITA_VEDA_ROSCA_ID;
            pedido1.price = 5;
            pedido1.quantity = 2;
            carrinho.add(pedido1);
        }
        final long idCompra = wait(this.dao.purchase(userId, carrinho));
        Assert.assertEquals(DBReset.ADMIN_SECOND_PURCHASE_ID + 1, idCompra);

        compras = wait(this.dao.loadPurchases(userId));
        Assert.assertTrue(compras != null && compras.size() == 3);
        Assert.assertTrue(compras.get(2).id == idCompra);
        Assert.assertTrue(compras.get(2).items.size() == 2);
        Assert.assertTrue(compras.get(2).total == 60);

        final ReceiptForm recibo = wait(this.dao.loadReceipt(idCompra));
        Assert.assertTrue(recibo != null);
        Assert.assertTrue(recibo.total == 60);
        Assert.assertTrue(recibo.items.size() == 2);

        Assert.assertTrue(recibo.items.get(0).value == pedido0.price);
        Assert.assertTrue(recibo.items.get(0).quantity == pedido0.quantity);
        Assert.assertTrue(recibo.items.get(0).description.equals("Pen Drive 2GB"));

        Assert.assertTrue(recibo.items.get(1).value == pedido1.price);
        Assert.assertTrue(recibo.items.get(1).quantity == pedido1.quantity);
        Assert.assertTrue(recibo.items.get(1).description.equals("Fita veda rosca"));
    }

}
