package br.com.wedocode.shopping.view.gwt.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;

public interface ShoppingDAOGwtAsync {

    void loadSubject(String username, String password, AsyncCallback<Subject> callback);

    void loadProducts(boolean fetchDescription, AsyncCallback<List<ProductItem>> callback);

    void loadPurchases(Long userId, AsyncCallback<List<PurchaseInfo>> callback);

    void loadProductById(Long productId, AsyncCallback<ProductItem> callback);

    void loadReceipt(Long purchaseId, AsyncCallback<ReceiptForm> callback);

    void purchase(Long userId, List<PurchaseItem> request, AsyncCallback<Long> callback);

}
