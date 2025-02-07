package br.com.wedocode.shopping.view.gwt.war;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import br.com.wedocode.shopping.business.jdbc.commands.LoadProductByIdCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadProductsCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadPurchasesCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadReceiptCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadSubjectCommand;
import br.com.wedocode.shopping.business.jdbc.commands.SavePurchaseCommand;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;
import br.com.wedocode.shopping.view.gwt.shared.ShoppingDAOGwt;

public class ShoppingDAOGwtServlet extends RemoteServiceServlet implements ShoppingDAOGwt {

    private static final long serialVersionUID = 8814370297509328136L;

    @Override
    public Subject loadSubject(String username, String password) throws DAOException {
        return LoadSubjectCommand.run(username, password);
    }

    @Override
    public List<ProductItem> loadProducts(boolean fetchDescription) throws DAOException {
        return LoadProductsCommand.run(fetchDescription);
    }

    @Override
    public List<PurchaseInfo> loadPurchases(Long userId) throws DAOException {
        return LoadPurchasesCommand.run(userId);
    }

    @Override
    public ProductItem loadProductById(Long productId) throws DAOException {
        return LoadProductByIdCommand.run(productId);
    }

    @Override
    public ReceiptForm loadReceipt(Long purchaseId) throws DAOException {
        return LoadReceiptCommand.run(purchaseId);
    }

    @Override
    public Long purchase(Long userId, List<PurchaseItem> request) throws DAOException {
        return SavePurchaseCommand.run(userId, request);
    }

}
