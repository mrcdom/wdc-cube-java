package br.com.wedocode.shopping.business.jdbc.commands;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.sql.DataSource;

import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.JDBCResultSetTemplate;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import br.com.wedocode.shopping.presentation.shared.business.struct.Subject;

public class LoadSubjectCommand {

    public static Subject run(String username, String password) throws DAOException {
        var cmd = new LoadSubjectCommand();
        cmd.username = username;
        cmd.password = password;
        cmd.execute();
        return cmd.subject;
    }

    // :: Injects

    private final DataSource ds = ShoppingServerContext.getDefaultDataSource();

    // :: Arguments

    public String username;

    public String password;

    // :: Output

    public Subject subject;

    public void execute() throws DAOException {
        if (username == null || password == null) {
            return;
        }

        var sql = "SELECT ID, NAME FROM EN_USER WHERE USERNAME=? AND PASSWORD=?";

        var FIELD_ID = 1;
        var FIELD_NAME = 2;

        var PARAM_USERNAME = 1;
        var PARAM_PASSWORD = 2;

        subject = new JDBCResultSetTemplate<Subject>(this.ds, sql)
                .prepare(pstmt -> {
                    pstmt.setString(PARAM_USERNAME, username);
                    var pwd = new BigInteger(MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8")))
                            .toString(36);
                    pstmt.setString(PARAM_PASSWORD, pwd);
                })
                .process(rs -> {
                    if (rs.next()) {
                        var subject = new Subject();
                        subject.setId(rs.getLong(FIELD_ID));
                        subject.setNinkName(rs.getString(FIELD_NAME));
                        return subject;
                    }
                    return null;
                })
                .fetch();
    }

}
