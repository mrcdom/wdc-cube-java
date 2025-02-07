package br.com.wedocode.shopping.business.jdbc.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtil {

    public static void close(Connection o) {
        if (o != null) {
            try {
                o.close();
            } catch (final SQLException e) {
                // no need to inform
            }
        }
    }

    public static void close(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (final IOException e) {
                // no need to inform
            }
        }
    }

    public static void close(Statement o) {
        if (o != null) {
            try {
                o.close();
            } catch (final SQLException e) {
                // no need to inform
            }
        }
    }

    public static void close(ResultSet o) {
        if (o != null) {
            try {
                o.close();
            } catch (final SQLException e) {
                // no need to inform
            }
        }
    }

    public static void setAutoCommit(Connection connection, boolean value) {
        try {
            connection.setAutoCommit(value);
        } catch (final SQLException e) {
            // no need to inform
        }
    }

    public static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            // no need to inform
        }
    }

}
