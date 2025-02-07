package br.com.wedocode.shopping.business.jdbc.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import br.com.wedocode.framework.commons.function.ThrowingConsumer;
import br.com.wedocode.framework.commons.function.ThrowingFunction;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOOfflineException;

public final class JDBCResultSetTemplate<T> {

    // :: Instance

    private final String sql;
    private final DataSource ds;
    private ThrowingConsumer<PreparedStatement> prepareCb;
    private ThrowingFunction<ResultSet, T> processCb;

    public JDBCResultSetTemplate(DataSource ds, String sql) {
        this.ds = ds;
        this.sql = sql;
    }

    public T fetch() throws DAOException {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            try {
                connection = ds.getConnection();
            } catch (Exception caught) {
                throw new DAOOfflineException(caught);
            }

            pstmt = connection.prepareStatement(sql);
            if (this.prepareCb != null) {
                this.prepareCb.accept(pstmt);
            }

            rs = pstmt.executeQuery();
            return this.processCb.apply(rs);
        } catch (final Exception e) {
            throw DAOException.wrap("Fetching data from RDBMS", e);
        } finally {
            JDBCUtil.close(rs);
            JDBCUtil.close(pstmt);
            JDBCUtil.close(connection);
        }
    }

    public JDBCResultSetTemplate<T> prepare(ThrowingConsumer<PreparedStatement> prepare) {
        this.prepareCb = prepare;
        return this;
    }

    public JDBCResultSetTemplate<T> process(ThrowingFunction<ResultSet, T> process) {
        this.processCb = process;
        return this;
    }

}