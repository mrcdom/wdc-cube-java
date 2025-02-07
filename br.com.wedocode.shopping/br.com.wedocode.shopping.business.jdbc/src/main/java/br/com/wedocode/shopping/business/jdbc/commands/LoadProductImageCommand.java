package br.com.wedocode.shopping.business.jdbc.commands;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCResultSetTemplate;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;

public class LoadProductImageCommand {

    public static byte[] loadProductImage(Long productId) throws DAOException {
        var cmd = new LoadProductImageCommand();
        cmd.productId = productId;
        cmd.execute();
        return cmd.imageBytes;
    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public Long productId;

    // :: Output

    public byte[] imageBytes;

    public void execute() throws DAOException {
        final String sql = "SELECT IMAGE FROM EN_PRODUCT WHERE ID=?";

        final int FIELD_IMAGE = 1;

        final int PARAM_ID = 1;

        imageBytes = new JDBCResultSetTemplate<byte[]>(this.ds, sql)
                .prepare(pstmt -> {
                    pstmt.setLong(PARAM_ID, productId);
                })
                .process(rs -> {
                    if (rs.next()) {
                        return rs.getBytes(FIELD_IMAGE);
                    }
                    return null;
                })
                .fetch();
    }

}
