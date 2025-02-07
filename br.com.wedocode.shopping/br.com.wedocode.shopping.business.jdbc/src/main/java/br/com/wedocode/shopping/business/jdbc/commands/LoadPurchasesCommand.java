package br.com.wedocode.shopping.business.jdbc.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCResultSetTemplate;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.struct.PurchaseInfo;

public class LoadPurchasesCommand {

    public static List<PurchaseInfo> run(Long userId) throws DAOException {
        var cmd = new LoadPurchasesCommand();
        cmd.userId = userId;
        cmd.execute();
        return cmd.purchaseList;

    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public Long userId;

    // :: Output

    public List<PurchaseInfo> purchaseList;

    public void execute() throws DAOException {
        if (userId == null) {
            purchaseList = new ArrayList<PurchaseInfo>();
            return;
        }

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT\n");
        sql.append("    B.ID,\n");
        sql.append("    B.BUYDATE,\n");
        sql.append("    P.NAME as PRODUCTNAME,\n");
        sql.append("    PI.AMOUNT,\n");
        sql.append("    PI.PRICE\n");
        sql.append("FROM\n");
        sql.append("    EN_PURCHASEITEM PI\n");
        sql.append("JOIN EN_PURCHASE B ON (PI.PURCHASEID = B.ID)\n");
        sql.append("JOIN EN_PRODUCT P ON (PI.PRODUCTID = P.ID)\n");
        sql.append("WHERE B.USERID = ?\n");

        final int FIELD_ID = 1;
        final int FIELD_BUYDATE = 2;
        final int FIELD_PRODUCTNAME = 3;
        final int FIELD_AMOUNT = 4;
        final int FIELD_PRICE = 5;

        final int PARAM_USERID = 1;

        this.purchaseList = new JDBCResultSetTemplate<List<PurchaseInfo>>(this.ds, sql.toString())
                .prepare(pstmt -> {
                    pstmt.setLong(PARAM_USERID, userId);
                })
                .process(rs -> {
                    final List<PurchaseInfo> list = new ArrayList<PurchaseInfo>();

                    final Map<Long, PurchaseInfo> purchaseMap = new HashMap<>();
                    while (rs.next()) {
                        final Long compraId = rs.getLong(FIELD_ID);
                        PurchaseInfo purchase = purchaseMap.get(compraId);
                        if (purchase == null || compraId != purchase.id) {
                            list.add(purchase = new PurchaseInfo());
                            purchase.id = compraId;
                            purchase.date = rs.getDate(FIELD_BUYDATE).getTime();
                            purchase.items = new ArrayList<String>();
                            purchase.total = 0;

                            purchaseMap.put(compraId, purchase);
                        }

                        purchase.items.add(rs.getString(FIELD_PRODUCTNAME));
                        purchase.total += rs.getInt(FIELD_AMOUNT) * rs.getDouble(FIELD_PRICE);
                    }
                    return list;
                })
                .fetch();
    }

}
