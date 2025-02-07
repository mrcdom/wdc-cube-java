package br.com.wedocode.shopping.business.jdbc.commands;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCResultSetTemplate;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;

public class LoadProductsCommand {

    public static List<ProductItem> run(boolean fetchDescription) throws DAOException {
        var cmd = new LoadProductsCommand();
        cmd.fetchDescription = fetchDescription;
        cmd.execute();
        return cmd.productList;
    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public boolean fetchDescription;

    // :: Output

    public List<ProductItem> productList;

    public void execute() throws DAOException {
        final String sql = "SELECT ID, NAME, PRICE" + (fetchDescription ? ", DESCRIPTION" : "") + " FROM EN_PRODUCT";

        final int FIELD_ID = 1;
        final int FIELD_NAME = 2;
        final int FIELD_PRICE = 3;
        final int FIELD_DESCRIPTION = 4;

        this.productList = new JDBCResultSetTemplate<List<ProductItem>>(this.ds, sql)
                .process(rs -> {
                    var list = new ArrayList<ProductItem>();
                    while (rs.next()) {
                        final ProductItem item = new ProductItem();
                        item.id = rs.getLong(FIELD_ID);
                        item.name = rs.getString(FIELD_NAME);
                        item.price = rs.getDouble(FIELD_PRICE);
                        item.image = "image/product/" + item.id + ".png";
                        if (fetchDescription) {
                            item.description = new String(rs.getBytes(FIELD_DESCRIPTION), "UTF-8");
                        }
                        list.add(item);
                    }
                    return list;
                })
                .fetch();
    }

}
