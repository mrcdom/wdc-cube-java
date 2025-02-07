package br.com.wedocode.shopping.view.gwt;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;
import br.com.wedocode.shopping.view.gwt.shared.ShoppingDAOGwtAsync;
import br.com.wedocode.shopping.view.gwt.util.AsyncCallbackHelper;

public class ShoppingDAOGwtDelegate implements ShoppingDAO {

    static Logger LOG = LoggerFactory.getLogger(ShoppingDAOGwtDelegate.class);

    private ShoppingDAOGwtAsync impl;

    public void setImpl(ShoppingDAOGwtAsync impl) {
        this.impl = impl;
    }

    @Override
    public Promise<Subject> loadSubject(String username, String password) {
        return new Promise<>((resolve, reject) -> {
            this.impl.loadSubject(username, password, new AsyncCallbackHelper<>(LOG, resolve, reject));
        });
    }

    @Override
    public Promise<List<ProductItem>> loadProducts(boolean fetchDescription) {
        return new Promise<>((resolve, reject) -> {
            this.impl.loadProducts(fetchDescription, new AsyncCallbackHelper<>(LOG, resolve, reject));
        });
    }

    @Override
    public Promise<List<PurchaseInfo>> loadPurchases(Long userId) {
        return new Promise<>((resolve, reject) -> {
            this.impl.loadPurchases(userId, new AsyncCallbackHelper<>(LOG, resolve, reject));
        });
    }

    @Override
    public Promise<ProductItem> loadProductById(Long productId) {
        return new Promise<>((resolve, reject) -> {
            this.impl.loadProductById(productId, new AsyncCallbackHelper<>(LOG, resolve, reject));
        });
    }

    @Override
    public Promise<ReceiptForm> loadReceipt(Long purchaseId) {
        return new Promise<>((resolve, reject) -> {
            this.impl.loadReceipt(purchaseId, new AsyncCallbackHelper<>(LOG, resolve, reject));
        });
    }

    @Override
    public Promise<Long> purchase(Long userId, List<PurchaseItem> request) {
        return new Promise<>((resolve, reject) -> {
            this.impl.purchase(userId, request, new AsyncCallbackHelper<>(LOG, resolve, reject));
        });
    }
}
