package br.com.wedocode.shopping.business.jdbc.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.output.StringBuilderWriter;

import br.com.wedocode.framework.commons.util.StringUtils;

public class DBCreate {

    private Connection connection;

    boolean mustResetDb = false;

    public DBCreate withConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public DBCreate withReset() {
        this.mustResetDb = true;
        return this;
    }

    public DBCreate run() throws Exception {
        Map<String, Boolean> tableMap = this.loadTableMap(this.connection.getMetaData());

        if (!tableMap.containsKey("PUBLIC.EN_USER")) {
            this.createTableUser();
            this.mustResetDb = true;
        }

        if (!tableMap.containsKey("PUBLIC.EN_PRODUCT")) {
            this.createTableProduct();
            this.mustResetDb = true;
        }

        if (!tableMap.containsKey("PUBLIC.EN_PURCHASE")) {
            this.createTablePurchase();
            this.mustResetDb = true;
        }

        if (!tableMap.containsKey("PUBLIC.EN_PURCHASEITEM")) {
            this.createTablePurchaseItem();
            this.mustResetDb = true;
        }

        if (this.mustResetDb) {
            DBReset.run(this.connection);
        }
        return this;
    }

    private Map<String, Boolean> loadTableMap(DatabaseMetaData metaData) throws Exception {
        final Map<String, Boolean> tableMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                final String tableSchem = rs.getString("TABLE_SCHEM");
                final String tableName = rs.getString("TABLE_NAME");

                StringBuilder sb = new StringBuilder();

                if (StringUtils.isNotBlank(tableSchem)) {
                    sb.append(tableSchem);
                    sb.append(".");
                }
                sb.append(tableName);

                tableMap.put(sb.toString(), Boolean.TRUE);
            }
        }
        return tableMap;
    }

    private void createTableUser() throws Exception {
        try (Statement stmt = this.connection.createStatement()) {
            StringBuilderWriter sql = new StringBuilderWriter();

            try (PrintWriter out = new PrintWriter(sql)) {
                out.println("CREATE TABLE IF NOT EXISTS EN_USER (");
                out.println("	ID BIGINT NOT NULL,");
                out.println("	USERNAME VARCHAR(255) NOT NULL,");
                out.println("	PASSWORD CHAR(32) NOT NULL,");
                out.println("	NAME VARCHAR(255) NOT NULL,");
                out.println("	CONSTRAINT PK_USER PRIMARY KEY (ID)");
                out.println(")");
            }

            stmt.execute(sql.toString());

            stmt.execute("CREATE SEQUENCE IF NOT EXISTS SQ_USER START WITH 1 INCREMENT BY 1");
            // MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE NOCACHE
        }
    }

    private void createTableProduct() throws Exception {
        try (Statement stmt = this.connection.createStatement()) {
            StringBuilderWriter sql = new StringBuilderWriter();

            try (PrintWriter out = new PrintWriter(sql)) {
                out.println("CREATE TABLE IF NOT EXISTS EN_PRODUCT (");
                out.println("	ID BIGINT NOT NULL,");
                out.println("	NAME VARCHAR_IGNORECASE(1000000) NOT NULL,");
                out.println("	PRICE NUMERIC(20,2) NOT NULL,");
                out.println("	DESCRIPTION BINARY(1000000) NOT NULL,");
                out.println("	IMAGE BINARY(1000000),");
                out.println("	CONSTRAINT PK_PRODUCT PRIMARY KEY (ID)");
                out.println(")");
            }

            stmt.execute(sql.toString());

            stmt.execute("CREATE SEQUENCE IF NOT EXISTS SQ_PRODUCT START WITH 1 INCREMENT BY 1");
        }
    }

    private void createTablePurchase() throws Exception {
        try (Statement stmt = this.connection.createStatement()) {
            StringBuilderWriter sql = new StringBuilderWriter();

            try (PrintWriter out = new PrintWriter(sql)) {
                out.println("CREATE TABLE IF NOT EXISTS EN_PURCHASE (");
                out.println("	ID BIGINT NOT NULL,");
                out.println("	USERID BIGINT NOT NULL,");
                out.println("	BUYDATE DATE NOT NULL,");
                out.println("	CONSTRAINT PK_PURCHASE PRIMARY KEY (ID),");
                out.println("	CONSTRAINT FK_PURCHASE_USER FOREIGN KEY (USERID) REFERENCES EN_USER(ID)");
                out.println(")");
            }

            stmt.execute(sql.toString());

            stmt.execute("CREATE SEQUENCE IF NOT EXISTS SQ_PURCHASE START WITH 1 INCREMENT BY 1");
        }
    }

    private void createTablePurchaseItem() throws Exception {
        try (Statement stmt = this.connection.createStatement()) {
            StringBuilderWriter sql = new StringBuilderWriter();

            try (PrintWriter out = new PrintWriter(sql)) {
                out.println("CREATE TABLE IF NOT EXISTS EN_PURCHASEITEM (");
                out.println("	ID BIGINT NOT NULL,");
                out.println("	PURCHASEID BIGINT NOT NULL,");
                out.println("	PRODUCTID BIGINT NOT NULL,");
                out.println("	AMOUNT INTEGER NOT NULL,");
                out.println("	PRICE NUMERIC(20,2) NOT NULL,");
                out.println("	CONSTRAINT PK_PURCHASEITEM PRIMARY KEY (ID),");
                out.println("	CONSTRAINT FK_PURCHASEITEM_PRODUCT FOREIGN KEY (PRODUCTID) REFERENCES EN_PRODUCT(ID),");
                out.println(
                        "	CONSTRAINT FK_PURCHASEITEM_PURCHASE FOREIGN KEY (PURCHASEID) REFERENCES EN_PURCHASE(ID)");
                out.println(")");
            }

            stmt.execute(sql.toString());

            stmt.execute("CREATE SEQUENCE IF NOT EXISTS SQ_PURCHASEITEM START WITH 1 INCREMENT BY 1");
        }
    }

}
