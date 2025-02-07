package br.com.wedocode.shopping.view.gwt.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;

@RemoteServiceRelativePath("dao")
public interface ShoppingDAOGwt extends RemoteService {

    Subject loadSubject(String username, String password) throws DAOException;

    List<ProductItem> loadProducts(boolean fetchDescription) throws DAOException;

    List<PurchaseInfo> loadPurchases(Long userId) throws DAOException;

    ProductItem loadProductById(Long productId) throws DAOException;

    ReceiptForm loadReceipt(Long purchaseId) throws DAOException;

    Long purchase(Long userId, List<PurchaseItem> request) throws DAOException;

}
