package br.com.wedocode.shopping.view.react.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.wedocode.shopping.business.DataSourceDelegate;
import br.com.wedocode.shopping.business.ScheduledExecutorServiceDelegate;
import br.com.wedocode.shopping.business.jdbc.util.DataSourceConfiguration;
import br.com.wedocode.shopping.view.react.java.javaURLContextFactory;
import br.com.wedocode.shopping.view.react.stub.Activator;
import br.com.wedocode.shopping.view.react.stub.endpoints.ProductImageServlet;
import br.com.wedocode.shopping.view.react.stub.endpoints.ViewStateServlet;
import br.com.wedocode.shopping.view.react.stub.endpoints.WdcStateDispatcherEndpoint;
import ch.qos.logback.classic.ClassicConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * LEIA: https://jetty.org/docs/jetty/12/programming-guide/server/websocket.html
 */
public class ShoppingReactJetty {

    private static Logger LOG;

    public static void main(String[] args) throws Exception {
        var app = new ShoppingReactJetty();

        app.baseDir = app.computeBaseDir();
        app.configDir = app.baseDir.resolve("configuration");
        app.dataDir = app.baseDir.resolve("data");
        app.logDir = app.baseDir.resolve("log");
        app.tempDir = app.baseDir.resolve("tmp");
        app.webAppsDir = app.baseDir.resolve("webapps");

        System.setProperty("user.dir", app.baseDir.toString());
        System.setProperty("jetty.base.dir", app.baseDir.toString());
        System.setProperty("jetty.data.dir", app.dataDir.toString());
        System.setProperty("jetty.temp.dir", app.tempDir.toString());
        System.setProperty("jetty.webapps.dir", app.webAppsDir.toString());
        System.setProperty("java.io.tmpdir", app.tempDir.toString());
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, app.configDir.resolve("logback.xml").toString());

        LOG = LoggerFactory.getLogger(ShoppingReactJetty.class);

        try {
            app.start();

            if (System.in != null) {
                LOG.info("Type \"stop\" and click ENTER to gracefully stop Jetty");

                try (var scanner = new Scanner(System.in)) {

                    // Ctrl + C will dispatch this handler
                    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

                    do {
                        final String line;
                        try {
                            line = scanner.next();
                        } catch (NoSuchElementException caught) {
                            break;
                        }

                        if ("stop".equals(line) || "close".equals(line) || "exit".equals(line)) {
                            break;
                        }
                    } while (true);
                }
            } else {
                final CountDownLatch stopLatch = new CountDownLatch(1);

                // Ctrl + C will dispatch this handler
                Runtime.getRuntime().addShutdownHook(new Thread(() -> stopLatch.countDown()));

                stopLatch.await();
            }
        } catch (Exception caught) {
            LOG.error("Starting", caught);
        } finally {
            app.stop();
        }
    }

    // Instance section

    private Path baseDir;

    private Path configDir;

    private Path dataDir;

    private Path logDir;

    private Path tempDir;

    private Path webAppsDir;

    private InitialContext jndiContext;

    private Server jettyServer;

    private ScheduledExecutorService scheduledExecutorServiceImpl;

    private ScheduledExecutorServiceDelegate scheduledExecutorServiceDelegate = new ScheduledExecutorServiceDelegate();

    private BasicDataSource shoppingDataSourceImpl;

    private DataSourceDelegate shoppingDataSourceDelegate = new DataSourceDelegate();

    private volatile boolean running;

    private void start() throws Exception {
        this.running = true;

        var gson = new GsonBuilder().create();

        var jettyConfig = this.loadJettyConfiguration(gson);
        var shoppingDsCfg = this.loadDataSourceConfiguration(gson, jettyConfig.getShoppingDataSource());

        this.initEnvironment();
        this.initJndi();
        this.initScheduledExecutor();
        this.initShoppingDatabase(shoppingDsCfg);
        this.initJettyServer(jettyConfig);

        final ServletContextHandler shoppingContext;
        {
            var contextName = "shopping-react";

            shoppingContext = new ServletContextHandler("/" + contextName);
            shoppingContext.addEventListener(new br.com.wedocode.shopping.view.react.stub.Activator());

            {
                URL urlStatics = Activator.class.getResource("/META-INF/resources/.keep");
                Objects.requireNonNull(urlStatics, "Unable to find .keep in classpath");
                String urlBase = urlStatics.toExternalForm().replaceFirst("/[^/]*$", "/");

                ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
                defHolder.setInitParameter("resourceBase", urlBase);
                defHolder.setInitParameter("dirAllowed", "true");
                shoppingContext.addServlet(defHolder, "/");

                LOG.info("resourceBase: " + urlBase);
            }

            {
                var cfg = ProductImageServlet.class.getAnnotation(WebServlet.class);
                shoppingContext.addServlet(ProductImageServlet.class, cfg.urlPatterns()[0]);
            }

            {
                var cfg = ViewStateServlet.class.getAnnotation(WebServlet.class);
                shoppingContext.addServlet(ViewStateServlet.class, cfg.urlPatterns()[0]);
            }
        }

        this.jettyServer.setHandler(shoppingContext);

        {
            var sessionHandler = new SessionHandler();
            sessionHandler.setMaxInactiveInterval((int) Duration.ofMinutes(3).toSeconds());
            shoppingContext.setSessionHandler(sessionHandler);
        }

        JakartaWebSocketServletContainerInitializer.configure(shoppingContext, (servletContext, container) -> {
            // Configure the ServerContainer.
            container.setDefaultMaxTextMessageBufferSize(128 * 1024);
            container.setDefaultMaxSessionIdleTimeout(Duration.ofMinutes(5).toMillis());

            var dispatcherInfo = WdcStateDispatcherEndpoint.class.getAnnotation(ServerEndpoint.class);
            try {
                ServerEndpointConfig.Configurator configurator = null;
                if (dispatcherInfo.configurator() != null) {
                    configurator = dispatcherInfo.configurator().getDeclaredConstructor().newInstance();
                }
                container.addEndpoint(
                        ServerEndpointConfig.Builder.create(WdcStateDispatcherEndpoint.class, dispatcherInfo.value())
                                .subprotocols(List.of(dispatcherInfo.subprotocols()))
                                .configurator(configurator)
                                .build());
            } catch (Exception e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        });

        this.jettyServer.start();
    }

    public void stop() {
        // Double check pattern
        if (this.running) {
            synchronized (this) {
                if (this.running) {
                    try {
                        this.doStop();
                    } finally {
                        this.running = false;
                    }
                }
            }
        }
    }

    private void doStop() {
        LOG.info("stopping server...");

        this.shoppingDataSourceDelegate.setImpl(null);
        if (this.shoppingDataSourceImpl != null) {
            try {
                this.shoppingDataSourceImpl.close();
            } catch (Throwable caught) {
                LOG.error("Stopping shoppingDatasourceImpl", caught);
            } finally {
                this.shoppingDataSourceImpl = null;
            }
        }

        if (this.jettyServer != null) {
            try {
                this.jettyServer.stop();
            } catch (Exception caught) {
                LOG.error("Stopping jettyServer", caught);
            } finally {
                this.jettyServer = null;
            }
        }

        if (this.jndiContext != null) {
            try {
                this.jndiContext.close();
            } catch (Exception caught) {
                LOG.error("Stopping jndiContext", caught);
            } finally {
                this.jndiContext = null;
            }
        }

        this.scheduledExecutorServiceDelegate.setImpl(null);
        if (this.scheduledExecutorServiceImpl != null) {
            try {
                this.scheduledExecutorServiceImpl.shutdown();
            } catch (Exception caught) {
                LOG.error("Stopping jettyServer", caught);
            } finally {
                this.scheduledExecutorServiceImpl = null;
            }
        }

        LOG.info("Server stopped");
        this.running = false;
    }

    private JettyConfiguration loadJettyConfiguration(Gson gson) throws IOException {
        var config = gson.fromJson(Files.readString(this.configDir.resolve("jetty.json"), StandardCharsets.UTF_8),
                JettyConfiguration.class);

        if (config.getMaxThreads() == null || config.getMaxThreads() <= 0) {
            config.setMaxThreads(200);
        }

        if (config.getMinThreads() == null || config.getMinThreads() <= 0) {
            config.setMinThreads(Runtime.getRuntime().availableProcessors());
        }

        if (config.getThreadTimeoutMillis() == null || config.getThreadTimeoutMillis() <= 0) {
            config.setThreadTimeoutMillis(60000);
        }

        if (StringUtils.isBlank(config.getHost())) {
            config.setHost("127.0.0.1");
        }

        if (config.getPort() == null || config.getPort() <= 0) {
            config.setPort(8080);
        }

        if (config.getIdleTimeout() == null || config.getIdleTimeout() <= 0) {
            config.setIdleTimeout(TimeUnit.HOURS.toMillis(1));
        }

        if (StringUtils.isBlank(config.getShoppingDataSource())) {
            config.setShoppingDataSource("default-ds-memory.json");
        }

        return config;
    }

    private DataSourceConfiguration loadDataSourceConfiguration(Gson gson, String dsConfigFileName) throws IOException {
        //@formatter:off
		var cfg = gson.fromJson(
			Files.readString(this.configDir.resolve(dsConfigFileName), StandardCharsets.UTF_8),
			DataSourceConfiguration.class
		);
		//@formatter:on

        return cfg;
    }

    private BasicDataSource newDataSource(DataSourceConfiguration cfg) throws Exception {
        if (cfg.getDriverClassName().contains(".h2.")) {
            var urlStartWith = "jdbc:h2:~/";
            if (cfg.getUrl().startsWith(urlStartWith)) {
                cfg.setUrl("jdbc:h2:" + this.dataDir.toString() + "/" + cfg.getUrl().substring(urlStartWith.length()));
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

    private void initEnvironment() throws Exception {
        System.setProperty("jboss.server.base.dir", this.baseDir.toString());
        System.setProperty("jboss.server.config.dir", this.configDir.toString());
        System.setProperty("jboss.server.data.dir", this.dataDir.toString());
        System.setProperty("jboss.server.log.dir", this.logDir.toString());
        System.setProperty("jboss.server.temp.dir", this.tempDir.toString());
    }

    private void initJndi() throws Exception {
        var emptyFolder = Files.createTempDirectory(this.tempDir, "empty");
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.SimpleJndiContextFactory");
            System.setProperty("java.naming.factory.url.pkgs", "br.com.wedocode.shopping.view.react");
            System.setProperty(SimpleJndi.SHARED, "true");
            System.setProperty(SimpleJndi.ROOT, emptyFolder.toString());
            System.setProperty(SimpleJndi.ENC, "files");
            System.setProperty(JndiLoader.DELIMITER, "/");

            this.jndiContext = new InitialContext();
            javaURLContextFactory.defaultContext = this.jndiContext.createSubcontext("java");

            javaURLContextFactory.bind("jboss/ee/concurrency/scheduler",
                    "default", this.scheduledExecutorServiceDelegate);

            javaURLContextFactory.bind("jboss/datasources",
                    "shopping", this.shoppingDataSourceDelegate);
        } finally {
            Files.deleteIfExists(emptyFolder);
        }
    }

    private void initScheduledExecutor() throws Exception {
        this.scheduledExecutorServiceImpl = Executors
                .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.scheduledExecutorServiceDelegate.setImpl(this.scheduledExecutorServiceImpl);
    }

    private void initShoppingDatabase(DataSourceConfiguration cfg) throws Exception {
        this.shoppingDataSourceImpl = this.newDataSource(cfg);
        this.shoppingDataSourceDelegate.setImpl(this.shoppingDataSourceImpl);

        if (cfg.isReset()) {
            System.setProperty("wedocode.shopping.db.reset", "true");
        }
    }

    private void initJettyServer(JettyConfiguration jettyConfig) {
        this.jettyServer = new Server(new QueuedThreadPool(jettyConfig.getMaxThreads(), jettyConfig.getMinThreads(),
                jettyConfig.getThreadTimeoutMillis()));

        final ServerConnector connector;
        {
            final HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSecureScheme("https");
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());

            connector = new ServerConnector(this.jettyServer, new HttpConnectionFactory(httpConfig));
            connector.setIdleTimeout(jettyConfig.getIdleTimeout());
            connector.setHost(jettyConfig.getHost());
            connector.setPort(jettyConfig.getPort());
        }

        this.jettyServer.setConnectors(new Connector[] { connector });
    }

    private Path computeBaseDir() throws IOException {
        var userDefinedPathStr = System.getProperty("jetty.base.dir", null);
        if (userDefinedPathStr == null) {
            var path = this.getJarFileFromRepo(this.getClass());
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
            var strPath = cls.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            return new File(strPath).getCanonicalFile().toPath();
        } catch (final Exception cause) {
            return null;
        }
    }

}
