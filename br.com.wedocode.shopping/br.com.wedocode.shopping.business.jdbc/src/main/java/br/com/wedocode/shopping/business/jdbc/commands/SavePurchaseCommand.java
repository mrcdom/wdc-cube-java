package br.com.wedocode.shopping.business.jdbc.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCUtil;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.business.struct.PurchaseItem;

public class SavePurchaseCommand {

    public static Long run(Long userId, List<PurchaseItem> request) throws DAOException {
        var cmd = new SavePurchaseCommand();
        cmd.userId = userId;
        cmd.request = request;
        cmd.execute();
        return cmd.purchaseId;
    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public Long userId;

    public List<PurchaseItem> request;

    // :: Output

    public Long purchaseId;

    public void execute() throws DAOException {
        final PurchaseBuilder builder = new PurchaseBuilder(this.ds);
        try {
            builder.begin();
            try {
                purchaseId = builder.newPurchase(userId, System.currentTimeMillis());

                for (final PurchaseItem purchaseItem : request) {
                    builder.addItem(purchaseId, purchaseItem.productId, purchaseItem.quantity, purchaseItem.price);
                }
            } finally {
                builder.end();
            }
        } catch (final Exception e) {
            throw DAOException.wrap("Saving purchase", e);
        }
    }

    private static class PurchaseBuilder {

        private final DataSource ds;
        private Connection connection;
        private PreparedStatement purchaseStatement;
        private PreparedStatement purchaseItemStatement;
        private boolean hasError;
        private boolean oldAutoCommit;

        public PurchaseBuilder(DataSource ds) {
            this.ds = ds;
        }

        public void begin() throws SQLException {
            this.hasError = false;
            this.connection = this.ds.getConnection();
            this.oldAutoCommit = this.connection.getAutoCommit();
            this.connection.setAutoCommit(false);
            this.purchaseStatement = this.connection
                    .prepareStatement("INSERT INTO EN_PURCHASE(ID, USERID, BUYDATE) VALUES (?, ?, ?)");
            this.purchaseItemStatement = this.connection
                    .prepareStatement("INSERT INTO EN_PURCHASEITEM(ID, PURCHASEID, PRODUCTID, AMOUNT, PRICE) "
                            + "VALUES (NEXT VALUE FOR SQ_PURCHASEITEM, ?, ?, ?, ?)");
        }

        public void end() throws SQLException {
            JDBCUtil.close(this.purchaseItemStatement);
            this.purchaseItemStatement = null;

            JDBCUtil.close(this.purchaseStatement);
            this.purchaseStatement = null;

            if (this.connection != null) {
                try {
                    if (this.hasError) {
                        JDBCUtil.rollback(this.connection);
                    } else {
                        this.connection.commit();
                    }
                } finally {
                    JDBCUtil.setAutoCommit(this.connection, this.oldAutoCommit);
                    JDBCUtil.close(this.connection);
                    this.connection = null;
                }
            }
        }

        private long nextPurchaseId() throws SQLException {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = this.connection.createStatement();
                rs = stmt.executeQuery("SELECT NEXT VALUE FOR SQ_PURCHASE FROM DUAL");
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("No record returned");
                }
            } finally {
                JDBCUtil.close(rs);
                JDBCUtil.close(stmt);
            }
        }

        public long newPurchase(long userId, long date) throws SQLException {
            try {
                final long id = this.nextPurchaseId();

                this.purchaseStatement.setLong(1, id);
                this.purchaseStatement.setLong(2, userId);
                this.purchaseStatement.setDate(3, new java.sql.Date(date));
                this.purchaseStatement.execute();

                return id;
            } catch (final SQLException exn) {
                this.hasError = true;
                throw exn;
            }
        }

        public void addItem(long purchaseId, long productId, int quantity, double price) throws SQLException {
            try {
                this.purchaseItemStatement.setLong(1, purchaseId);
                this.purchaseItemStatement.setLong(2, productId);
                this.purchaseItemStatement.setInt(3, quantity);
                this.purchaseItemStatement.setDouble(4, price);
                this.purchaseItemStatement.execute();
            } catch (final SQLException exn) {
                this.hasError = true;
                throw exn;
            }
        }

    }
}
