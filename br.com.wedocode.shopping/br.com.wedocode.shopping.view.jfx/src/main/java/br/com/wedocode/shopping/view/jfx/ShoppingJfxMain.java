package br.com.wedocode.shopping.view.jfx;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.concurrent.ScheduledExecutorDelegate;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.commons.util.Rethrow;
import br.com.wedocode.framework.commons.util.StringUtils;
import br.com.wedocode.shopping.business.DataSourceDelegate;
import br.com.wedocode.shopping.business.ScheduledExecutorServiceDelegate;
import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.ShoppingDAOJdbcImpl;
import br.com.wedocode.shopping.business.jdbc.util.DBCreate;
import br.com.wedocode.shopping.business.jdbc.util.DataSourceConfiguration;
import br.com.wedocode.shopping.presentation.PlaceParameters;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.presenter.Routes;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAODelegate;
import ch.qos.logback.classic.ClassicConstants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class ShoppingJfxMain extends javafx.application.Application {

    private static Logger LOG;

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }

    /*
     * Fields
     */

    private ScheduledExecutorService scheduledExecutorServiceImpl;

    private ScheduledExecutorServiceDelegate scheduledExecutorServiceDelegate = new ScheduledExecutorServiceDelegate();

    private BasicDataSource shoppingDataSourceImpl;

    private DataSourceDelegate shoppingDataSourceDelegate = new DataSourceDelegate();

    private ScheduledExecutor scheduledExecutorImpl;

    private ScheduledExecutorDelegate scheduledExecutorDelegate = new ScheduledExecutorDelegate();

    private ShoppingDAOJdbcImpl shoppingDAOImpl;

    private ShoppingDAODelegate shoppingDAODelegate = new ShoppingDAODelegate();

    private ShoppingJfxApplication app;

    /*
     *
     */

    public ShoppingJfxMain() {
        ShoppingServerContext.Internals.setDefaultDataSource(this.shoppingDataSourceDelegate);
        ShoppingServerContext.Internals.setScheduledExecutor(this.scheduledExecutorDelegate);

        ShoppingContext.Internals.setDAO(this.shoppingDAODelegate);
        ShoppingContext.Internals.setExecutor(this.scheduledExecutorDelegate);
    }

    @Override
    public void init() throws Exception {
        this.initEnvironment();

        LOG = LoggerFactory.getLogger(ShoppingJfxMain.class);
        LOG.info("Initializing...");

        var gson = new GsonBuilder().create();

        var config = this.loadConfiguration(gson);

        this.initScheduledExecutor();
        this.initShoppingDatabase(this.loadDataSourceConfiguration(gson, config.getShoppingDataSource()));
        this.initShoppingDAO();
    }

    @Override
    public void stop() throws Exception {
        LOG.info("finalizing...");

        if (this.app != null) {
            try {
                this.app.release();
            } catch (Throwable caught) {
                LOG.error("Closing Data Source Shopping", caught);
            }
            this.app = null;
        }

        this.shoppingDAODelegate.setImpl(null);
        this.shoppingDAOImpl = null;

        this.scheduledExecutorDelegate.setImpl(null);
        this.scheduledExecutorImpl = null;

        this.shoppingDataSourceDelegate.setImpl(null);
        if (this.shoppingDataSourceImpl != null) {
            try {
                this.shoppingDataSourceImpl.close();
            } catch (Throwable caught) {
                LOG.error("Closing Data Source Shopping", caught);
            }
        }

        this.scheduledExecutorServiceDelegate.setImpl(null);
        if (this.scheduledExecutorServiceImpl != null) {
            try {
                this.scheduledExecutorServiceImpl.shutdown();
            } catch (Throwable caught) {
                LOG.error("Doing a shutdown on Scheduled Executor Service", caught);
            }
            this.scheduledExecutorServiceImpl = null;
        }
    }

    private void initEnvironment() throws Exception {
        var baseDir = this.computeBaseDir();
        var configDir = baseDir.resolve("configuration");
        var dataDir = baseDir.resolve("data");
        var logDir = baseDir.resolve("log");
        var tempDir = baseDir.resolve("tmp");

        ShoppingServerContext.Internals.setBaseDir(baseDir);
        ShoppingServerContext.Internals.setConfigDir(configDir);
        ShoppingServerContext.Internals.setDataDir(dataDir);
        ShoppingServerContext.Internals.setLogDir(logDir);
        ShoppingServerContext.Internals.setTempDir(tempDir);

        System.setProperty("jboss.server.base.dir", baseDir.toString());
        System.setProperty("jboss.server.config.dir", configDir.toString());
        System.setProperty("jboss.server.data.dir", dataDir.toString());
        System.setProperty("jboss.server.log.dir", logDir.toString());
        System.setProperty("jboss.server.temp.dir", tempDir.toString());

        System.setProperty("user.dir", baseDir.toString());
        System.setProperty("java.io.tmpdir", tempDir.toString());

        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, configDir.resolve("logback.xml").toString());
    }

    private void initScheduledExecutor() throws Exception {
        this.scheduledExecutorServiceImpl = Executors
                .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.scheduledExecutorServiceDelegate.setImpl(this.scheduledExecutorServiceImpl);

        this.scheduledExecutorImpl = new ScheduledExecutorJfxAdapter(this.scheduledExecutorServiceDelegate);
        this.scheduledExecutorDelegate.setImpl(this.scheduledExecutorImpl);
    }

    private void initShoppingDatabase(DataSourceConfiguration cfg) throws Exception {
        this.shoppingDataSourceImpl = this.newDataSource(cfg);
        this.shoppingDataSourceDelegate.setImpl(this.shoppingDataSourceImpl);

        try (var connection = this.shoppingDataSourceImpl.getConnection()) {
            var cmd = new DBCreate().withConnection(connection);
            if (cfg.isReset()) {
                cmd.withReset();
            }
            cmd.run();
        } catch (final Exception caught) {
            Rethrow.emit(caught);
        }
    }

    private void initShoppingDAO() {
        this.shoppingDAOImpl = new ShoppingDAOJdbcImpl();
        this.shoppingDAODelegate.setImpl(this.shoppingDAOImpl);
    }

    private BasicDataSource newDataSource(DataSourceConfiguration cfg) throws Exception {
        if (cfg.getDriverClassName().contains(".h2.")) {
            var urlStartWith = "jdbc:h2:~/";
            if (cfg.getUrl().startsWith(urlStartWith)) {
                var dataDir = ShoppingServerContext.getDataDir();
                cfg.setUrl("jdbc:h2:" + dataDir.toString() + "/" + cfg.getUrl().substring(urlStartWith.length()));
            }
        }

        final var ds = new BasicDataSource();
        ds.setDriverClassName(cfg.getDriverClassName());
        ds.setUrl(cfg.getUrl());
        ds.setUsername(cfg.getUserName());
        ds.setPassword(cfg.getPassword());

        if (cfg.getInitialSize() != null) {
            ds.setInitialSize(cfg.getInitialSize());
        }

        if (cfg.getMaxActive() != null) {
            ds.setMaxActive(cfg.getMaxActive());
        }

        if (cfg.getMaxIdle() != null) {
            ds.setMaxIdle(cfg.getMaxIdle());
        }

        if (StringUtils.isNotBlank(cfg.getValidationQuery())) {
            ds.setValidationQuery(cfg.getValidationQuery());
        }

        return ds;
    }

    private Path computeBaseDir() throws IOException {
        var userDefinedPathStr = System.getProperty("app.base.dir", null);
        if (userDefinedPathStr == null) {
            userDefinedPathStr = System.getProperty("jboss.server.base.dir", null);
        }

        if (userDefinedPathStr == null) {
            var path = Path.of("configuration");
            if (Files.exists(path)) {
                return path.getParent().toRealPath().toAbsolutePath();
            }

            path = this.getJarFileFromRepo(this.getClass());
            if (path != null && path.endsWith("target/classes")) {
                path = path.getParent().getParent().resolve("work");
            } else {
                path = Path.of("work").toRealPath().toAbsolutePath();
            }
            return path;
        } else {
            return Path.of(userDefinedPathStr).normalize().toRealPath().toAbsolutePath();
        }
    }

    private Path getJarFileFromRepo(final Class<?> cls) {
        try {
            //@formatter:off
			var strPath = cls.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.toURI()
				.getPath();

			return new File(strPath).getCanonicalFile().toPath();
			//@formatter:on
        } catch (final Exception cause) {
            return null;
        }
    }

    private ShoppingJfxConfiguration loadConfiguration(Gson gson) throws IOException {
        var configDir = ShoppingServerContext.getConfigDir();

        var config = gson.fromJson(Files.readString(configDir.resolve("shopping.json"), StandardCharsets.UTF_8),
                ShoppingJfxConfiguration.class);

        if (StringUtils.isBlank(config.getShoppingDataSource())) {
            config.setShoppingDataSource("default-ds-memory.json");
        }

        return config;
    }

    private DataSourceConfiguration loadDataSourceConfiguration(Gson gson, String dsConfigFileName) throws IOException {
        var configDir = ShoppingServerContext.getConfigDir();

        //@formatter:off
		var cfg = gson.fromJson(
			Files.readString(configDir.resolve(dsConfigFileName), StandardCharsets.UTF_8),
			DataSourceConfiguration.class
		);
		//@formatter:on

        return cfg;
    }

    @Override
    public void start(Stage stage) {
        this.app = new ShoppingJfxApplication();

        Routes.login(this.app);

        // develToToProduct();
        // develToToReceipt();
        // develToToCart();

        var body = new ScrollPane();
        body.getStyleClass().add("body");
        body.setContent(this.app.getRootElement());

        var scene = new Scene(body, 1024, 768, false, SceneAntialiasing.BALANCED);
        scene.getStylesheets().add(getClass().getResource("/META-INF/resources/shopping.fx-css").toExternalForm());
        stage.setScene(scene);
        this.app.start();
        stage.show();
    }

    protected Promise<Boolean> develToTRestricted() {
        return Promise.resolve(Boolean.TRUE)
                //
                .then(ok -> Routes.login(this.app))
                //
                .then(ok -> {
                    var loginPresenter = this.app.getPresenterByClass(LoginPresenter.class);
                    loginPresenter.getState().userName = "admin";
                    loginPresenter.getState().password = "admin";
                    return loginPresenter.onEnter();
                });
    }

    protected Promise<Boolean> develToToProduct() {
        return develToTRestricted()
                //
                .then(ok -> {
                    var place = this.app.newPlace();
                    place.setParameter(PlaceParameters.PRODUCT_ID, 1L);
                    return Routes.product(this.app, place);
                });
    }

    protected Promise<Boolean> develToToReceipt() {
        return develToTRestricted()
                //
                .then(ok -> {
                    var place = this.app.newPlace();
                    place.setParameter(PlaceParameters.PURCHASE_ID, 1L);
                    return Routes.receipt(this.app, place);
                })
                //
                .then(ok -> {
                    var receiptPresenter = this.app.getPresenterByClass(ReceiptPresenter.class);
                    receiptPresenter.getState().notifySuccess = false;
                    return null;
                });
    }

    protected void develToToCart() {
        develToTRestricted()
                //
                .then(ok -> {
                    var place = this.app.newPlace();
                    place.setParameter(PlaceParameters.PRODUCT_ID, 1L);
                    return Routes.product(this.app, place);
                })
                //
                .then(ok -> {
                    var productPresenter = this.app.getPresenterByClass(ProductPresenter.class);
                    return productPresenter.onAddToCart(1);
                })
                //
                .then(ok -> {
                    var cartPresenter = this.app.getPresenterByClass(CartPresenter.class);
                    return cartPresenter.onOpenProducts();
                })
                //
                .then(ok -> {
                    var place = this.app.newPlace();
                    place.setParameter(PlaceParameters.PRODUCT_ID, 2L);
                    return Routes.product(this.app, place);
                })
                //
                .then(ok -> {
                    var productPresenter = this.app.getPresenterByClass(ProductPresenter.class);
                    return productPresenter.onAddToCart(1);
                })
                //
                .then(ok -> {
                    var cartPresenter = this.app.getPresenterByClass(CartPresenter.class);
                    // Simulates an error message
                    cartPresenter.getState().errorCode = 1;
                    cartPresenter.getState().errorMessage = "Simulated error message";
                    return null;
                });
    }

}
