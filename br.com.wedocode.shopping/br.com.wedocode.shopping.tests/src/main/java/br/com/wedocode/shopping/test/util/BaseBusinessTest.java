package br.com.wedocode.shopping.test.util;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.commons.util.Rethrow;
import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.ShoppingDAOJdbcImpl;
import br.com.wedocode.shopping.business.jdbc.util.DBCreate;
import br.com.wedocode.shopping.business.jdbc.util.JDBCUtil;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;

public class BaseBusinessTest {

    private static BasicDataSource datasource;

    protected static ScheduledExecutorForTest executor;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // executor = new ScheduledExecutorForTestSyncDirect();
        executor = new ScheduledExecutorForTestSyncDelayed();
        // executor = new ScheduledExecutorForTestAsync(10);

        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
        ds.setUrl("jdbc:h2:mem:wedocode-shopping;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        ds.setUsername("sa");
        ds.setPassword("sa");
        ds.setInitialSize(1);
        ds.setMaxActive(10);
        ds.setMaxIdle(5);
        ds.setValidationQuery("SELECT 1 FROM DUAL");
        BaseBusinessTest.datasource = ds;

        var basePath = Paths.get("work");

        ShoppingServerContext.Internals.setBaseDir(basePath);
        ShoppingServerContext.Internals.setConfigDir(basePath.resolve("config"));
        ShoppingServerContext.Internals.setDataDir(basePath.resolve("data"));
        ShoppingServerContext.Internals.setLogDir(basePath.resolve("log"));
        ShoppingServerContext.Internals.setTempDir(basePath.resolve("temp"));

        ShoppingServerContext.Internals.setDefaultDataSource(ds);
        ShoppingServerContext.Internals.setScheduledExecutor(executor);

        ShoppingContext.Internals.setDAO(new ShoppingDAOJdbcImpl());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ShoppingContext.Internals.setDAO(null);
        BaseBusinessTest.datasource.close();
        BaseBusinessTest.datasource = null;
        executor.shutdown();
    }

    protected <T> T wait(Promise<T> promise) throws Exception {
        AtomicReference<T> valueRef = new AtomicReference<>();
        AtomicReference<Throwable> caughtRef = new AtomicReference<>();

        promise.then(value -> {
            valueRef.set(value);

            return Promise.resolve(value);
        });

        promise.catch_(caught -> {
            caughtRef.set(caught);
            return Promise.reject(caught);
        });

        executor.flush();

        if (caughtRef.get() instanceof Exception) {
            return Rethrow.emit(caughtRef.get());
        }

        return valueRef.get();
    }

    /*
     * Instance
     */

    protected ShoppingDAO dao;

    @Before
    public void before() throws Exception {
        this.dao = ShoppingContext.getDAO();

        Connection connection = null;
        try {
            connection = BaseBusinessTest.datasource.getConnection();
            //@formatter:off
			new DBCreate()
				.withConnection(connection)
				.withReset()
				.run();
			//@formatter:on
        } finally {
            JDBCUtil.close(connection);
        }
    }

    @After
    public void after() {
        this.dao = null;
    }

}
