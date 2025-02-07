package br.com.wedocode.shopping.business.jdbc;

import java.util.List;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.commands.LoadProductByIdCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadProductsCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadPurchasesCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadReceiptCommand;
import br.com.wedocode.shopping.business.jdbc.commands.LoadSubjectCommand;
import br.com.wedocode.shopping.business.jdbc.commands.SavePurchaseCommand;
import br.com.wedocode.shopping.business.jdbc.util.ReentrantReadWriteLockExt;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;

public class ShoppingDAOJdbcImpl implements ShoppingDAO {

    private final ScheduledExecutor executor;

    private final ReentrantReadWriteLockExt rrwl;

    public ShoppingDAOJdbcImpl() {
        this.executor = ShoppingServerContext.getScheduledExecutor();
        this.rrwl = new ReentrantReadWriteLockExt(this.executor);
    }

    public void close() {
        // NOOP
    }

    @Override
    public Promise<Subject> loadSubject(String username, String password) {
        return this.rrwl.readLockPromise(() -> LoadSubjectCommand.run(username, password));
    }

    @Override
    public Promise<List<ProductItem>> loadProducts(boolean fetchDescription) {
        return this.rrwl.readLockPromise(() -> LoadProductsCommand.run(fetchDescription));
    }

    @Override
    public Promise<List<PurchaseInfo>> loadPurchases(Long userId) {
        return this.rrwl.readLockPromise(() -> LoadPurchasesCommand.run(userId));
    }

    @Override
    public Promise<ProductItem> loadProductById(Long productId) {
        return this.rrwl.readLockPromise(() -> LoadProductByIdCommand.run(productId));
    }

    @Override
    public Promise<ReceiptForm> loadReceipt(Long purchaseId) {
        return this.rrwl.readLockPromise(() -> LoadReceiptCommand.run(purchaseId));
    }

    @Override
    public Promise<Long> purchase(Long userId, List<PurchaseItem> request) {
        return this.rrwl.writeLockPromise(() -> SavePurchaseCommand.run(userId, request));
    }

}
