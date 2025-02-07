package br.com.wedocode.shopping.presentation.presenter.restricted;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.business.exception.InvalidCartItemDAOException;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.CartItem;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;

public class CartManager {

    private final ShoppingApplication app;

    private List<CartItem> cart;

    private ShoppingDAO dao;

    private int listenerIdGen;
    private Map<Integer, ThrowingRunnable> commitListenerMap;
    private Map<Integer, ThrowingRunnable> changeListenerMap;

    public CartManager(ShoppingApplication app, ShoppingDAO dao) {
        this.app = app;
        this.dao = dao;
        this.cart = new ArrayList<CartItem>();
        this.commitListenerMap = new HashMap<>();
        this.changeListenerMap = new HashMap<>();
    }

    public ThrowingRunnable addCommitListener(ThrowingRunnable listener) {
        var listenerID = listenerIdGen++;
        this.commitListenerMap.put(listenerID, listener);
        return () -> {
            this.commitListenerMap.remove(listenerID);
        };
    }

    public ThrowingRunnable addChangeListener(ThrowingRunnable listener) {
        var listenerID = listenerIdGen++;
        this.changeListenerMap.put(listenerID, listener);
        return () -> {
            this.changeListenerMap.remove(listenerID);
        };
    }

    public List<CartItem> getCartItems() {
        return Collections.unmodifiableList(this.cart);
    }

    public void addProduct(ProductItem product, int quantity) {
        boolean isNew = true;
        for (final CartItem item : this.cart) {
            if (item.id == product.id) {
                item.quantity += quantity;
                isNew = false;
                break;
            }
        }

        if (isNew) {
            final CartItem item = new CartItem();
            item.id = product.id;
            item.name = product.name;
            item.image = product.image;
            item.price = product.price;
            item.quantity = quantity;
            this.cart.add(item);

            for (var listener : new ArrayList<>(this.changeListenerMap.values())) {
                listener.run();
            }
        }
    }

    public boolean modifyProductQuantity(long productId, int quantity) {
        boolean found = false;

        final Iterator<CartItem> it = this.cart.iterator();
        while (it.hasNext()) {
            final CartItem cartItem = it.next();
            if (cartItem.id == productId) {
                cartItem.quantity = quantity;
                found = true;
                break;
            }
        }

        if (found) {
            for (var listener : new ArrayList<>(this.changeListenerMap.values())) {
                listener.run();
            }
        }

        return found;
    }

    public boolean removeProduct(long productId) {
        boolean modified = false;

        final Iterator<CartItem> it = this.cart.iterator();
        while (it.hasNext()) {
            final CartItem cartItem = it.next();
            if (cartItem.id == productId) {
                it.remove();
                modified = true;
                break;
            }
        }

        if (modified) {
            for (var listener : new ArrayList<>(this.changeListenerMap.values())) {
                listener.run();
            }
        }

        return modified;
    }

    public Promise<Long> commit(Subject subject) {
        final List<PurchaseItem> request;
        try {
            request = new ArrayList<PurchaseItem>(this.cart.size());

            for (final CartItem cartItem : this.cart) {
                final PurchaseItem purchaseItem = new PurchaseItem();
                purchaseItem.productId = cartItem.id;
                purchaseItem.price = cartItem.price;
                purchaseItem.quantity = cartItem.quantity;
                if (cartItem.quantity < 0) {
                    throw new InvalidCartItemDAOException();
                }
                request.add(purchaseItem);
            }
        } catch (Throwable caught) {
            return Promise.reject(caught);
        }

        return this.dao.purchase(subject.getId(), request)

                // OnSuccess
                .then(purchaseId -> {
                    synchronized (app) {
                        this.clear();

                        for (var listener : new ArrayList<>(this.commitListenerMap.values())) {
                            listener.run();
                        }

                        return Promise.resolve(purchaseId);
                    }
                });
    }

    public int getSize() {
        return this.cart.size();
    }

    public void clear() {
        this.cart.clear();
    }

}
