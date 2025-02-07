package br.com.wedocode.shopping.presentation.shared.business;

import java.util.List;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;

public class ShoppingDAODelegate implements ShoppingDAO {

    private ShoppingDAO impl;

    public ShoppingDAODelegate() {
        impl = ShoppingDAOUnavailable.INSTANCE;
    }

    public void setImpl(ShoppingDAO impl) {
        this.impl = impl != null ? impl : ShoppingDAOUnavailable.INSTANCE;
    }

    public Promise<Subject> loadSubject(String username, String password) {
        return impl.loadSubject(username, password);
    }

    public Promise<List<ProductItem>> loadProducts(boolean fetchDescription) {
        return impl.loadProducts(fetchDescription);
    }

    public Promise<List<PurchaseInfo>> loadPurchases(Long userId) {
        return impl.loadPurchases(userId);
    }

    public Promise<ProductItem> loadProductById(Long productId) {
        return impl.loadProductById(productId);
    }

    public Promise<ReceiptForm> loadReceipt(Long purchaseId) {
        return impl.loadReceipt(purchaseId);
    }

    public Promise<Long> purchase(Long userId, List<PurchaseItem> request) {
        return impl.purchase(userId, request);
    }

    private static class ShoppingDAOUnavailable implements ShoppingDAO {

        static final ShoppingDAOUnavailable INSTANCE = new ShoppingDAOUnavailable();

        private <V> Promise<V> notAvailable() {
            return new Promise<V>((resolve, reject) -> {
                reject.accept(new Exception("Service Unavailable"));
            });
        }

        @Override
        public Promise<Subject> loadSubject(String username, String password) {
            return notAvailable();
        }

        @Override
        public Promise<List<ProductItem>> loadProducts(boolean fetchDescription) {
            return notAvailable();
        }

        @Override
        public Promise<List<PurchaseInfo>> loadPurchases(Long userId) {
            return notAvailable();
        }

        @Override
        public Promise<ProductItem> loadProductById(Long productId) {
            return notAvailable();
        }

        @Override
        public Promise<ReceiptForm> loadReceipt(Long purchaseId) {
            return notAvailable();
        }

        @Override
        public Promise<Long> purchase(Long userId, List<PurchaseItem> request) {
            return notAvailable();
        }

    }
}
