package br.com.wedocode.shopping.business.jdbc.commands;

import java.util.ArrayList;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCResultSetTemplate;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptForm;
import br.com.wedocode.shopping.presentation.shared.struct.ReceiptItem;

public class LoadReceiptCommand {

    public static ReceiptForm run(Long purchaseId) throws DAOException {
        var cmd = new LoadReceiptCommand();
        cmd.purchaseId = purchaseId;
        cmd.execute();
        return cmd.receipt;
    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public Long purchaseId;

    // :: Output

    public ReceiptForm receipt;

    public void execute() throws DAOException {
        final var sql = new StringBuilder();
        sql.append("SELECT\n");
        sql.append("    B.BUYDATE,\n");
        sql.append("    P.NAME as PRODUCTNAME,\n");
        sql.append("    PI.AMOUNT,\n");
        sql.append("    PI.PRICE\n");
        sql.append("FROM\n");
        sql.append("    EN_PURCHASEITEM PI\n");
        sql.append("JOIN EN_PURCHASE B ON (PI.PURCHASEID = B.ID)\n");
        sql.append("JOIN EN_PRODUCT P ON (PI.PRODUCTID = P.ID)\n");
        sql.append("WHERE B.ID = ?\n");

        final int FIELD_BUYDATE = 1;
        final int FIELD_PRODUCTNAME = 2;
        final int FIELD_AMOUNT = 3;
        final int FIELD_PRICE = 4;

        final int PARAM_ID = 1;

        this.receipt = new JDBCResultSetTemplate<ReceiptForm>(this.ds, sql.toString())
                .prepare(pstmt -> {
                    pstmt.setLong(PARAM_ID, purchaseId);
                })
                .process(rs -> {
                    ReceiptForm form = null;
                    while (rs.next()) {
                        if (form == null) {
                            form = new ReceiptForm();
                            form.date = rs.getDate(FIELD_BUYDATE).getTime();
                            form.total = 0;
                            form.items = new ArrayList<ReceiptItem>();
                        }

                        final ReceiptItem item = new ReceiptItem();
                        item.description = rs.getString(FIELD_PRODUCTNAME);
                        item.quantity = rs.getInt(FIELD_AMOUNT);
                        item.value = rs.getDouble(FIELD_PRICE);
                        form.items.add(item);

                        form.total += item.quantity * item.value;
                    }
                    return form;
                })
                .fetch();
    }

}
