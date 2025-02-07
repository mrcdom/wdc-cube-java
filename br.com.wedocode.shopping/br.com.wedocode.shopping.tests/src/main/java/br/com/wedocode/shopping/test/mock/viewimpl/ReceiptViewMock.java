package br.com.wedocode.shopping.test.mock.viewimpl;

import java.util.Date;

import org.junit.Assert;

import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptItem;
import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class ReceiptViewMock extends AbstractViewMock<ReceiptPresenter> {

    public static ReceiptViewMock cast(WebFlowView view) {
        var cls = ReceiptViewMock.class;
        Assert.assertNotNull("Expecting " + cls.getSimpleName() + " but this view was null", view);
        Assert.assertTrue("Expecting " + cls.getSimpleName() + " but it was " + view.getClass().getSimpleName(),
                cls.isInstance(view));
        return (ReceiptViewMock) view;
    }

    public ReceiptPresenter.ReceiptState state;

    public ReceiptViewMock(ShoppingApplicationMock app, ReceiptPresenter presenter) {
        super(app, presenter);
        this.state = presenter.getState();
    }

    public void printRecibo_() {
        if (this.state.notifySuccess) {
            System.out.println("Compra efetuada com sucesso");
            System.out.println();
        }

        System.out.println("Imprima seu recibo:");

        System.out.println("------------------------------------------------------------");
        System.out.println("TR�PLICE SHOPPING - SUA COMPRA CERTA NA INTERNET");
        System.out.println("Recibo de compra");
        System.out.println("Data: " + new Date(this.state.receipt.date));
        System.out.println("------------------------------------------------------------");
        for (final ReceiptItem item : this.state.receipt.items) {
            System.out.print(item.description);
            System.out.print("(" + item.quantity + ")=R$ ");
            System.out.println(item.value);
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("TOTAL: " + this.state.receipt.total);

    }

}
