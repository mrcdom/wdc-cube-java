package br.com.wedocode.shopping.presentation.shared.business;

import java.util.List;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;

public interface ShoppingDAO {

    Promise<Subject> loadSubject(String username, String password);

    Promise<List<ProductItem>> loadProducts(boolean fetchDescription);

    Promise<List<PurchaseInfo>> loadPurchases(Long userId);

    Promise<ProductItem> loadProductById(Long productId);

    Promise<ReceiptForm> loadReceipt(Long purchaseId);

    Promise<Long> purchase(Long userId, List<PurchaseItem> request);

}
