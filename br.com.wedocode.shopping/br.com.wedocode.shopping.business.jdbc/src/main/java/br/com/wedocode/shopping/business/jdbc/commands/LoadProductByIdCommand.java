package br.com.wedocode.shopping.business.jdbc.commands;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCResultSetTemplate;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.struct.ProductItem;

public class LoadProductByIdCommand {

    public static ProductItem run(Long productId) throws DAOException {
        var cmd = new LoadProductByIdCommand();
        cmd.productId = productId;
        cmd.execute();
        return cmd.productItem;
    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public Long productId;

    // :: Output

    public ProductItem productItem;

    public void execute() throws DAOException {
        if (productId == null) {
            return;
        }

        final String sql = "SELECT NAME, PRICE, DESCRIPTION FROM EN_PRODUCT WHERE ID=?";

        final int FIELD_NAME = 1;
        final int FIELD_PRICE = 2;
        final int FIELD_DESCRIPTION = 3;

        final int PARAM_ID = 1;

        productItem = new JDBCResultSetTemplate<ProductItem>(this.ds, sql)
                .prepare(pstmt -> {
                    pstmt.setLong(PARAM_ID, productId);
                })
                .process(rs -> {
                    if (rs.next()) {
                        final ProductItem product = new ProductItem();
                        product.id = productId;
                        product.image = "image/product/" + productId + ".png";
                        product.name = rs.getString(FIELD_NAME);
                        product.price = rs.getDouble(FIELD_PRICE);
                        product.description = new String(rs.getBytes(FIELD_DESCRIPTION), "UTF-8");
                        return product;
                    }
                    return null;
                }).fetch();
    }

}
