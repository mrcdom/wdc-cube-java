package br.com.wedocode.shopping.view.gwt.war;

import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;

import javax.naming.InitialContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import javax.sql.DataSource;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutorDelegate;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.util.Rethrow;
import br.com.wedocode.shopping.business.ScheduledExecutorAdapter;
import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.util.DBCreate;

public class Activator implements ServletContextListener {

    private ServletContext context;

    private ScheduledExecutorService scheduledExecutorService;
    private DataSource defaultDataSource;

    private ScheduledExecutorDelegate scheduledExecutorDelegate = new ScheduledExecutorDelegate(null);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();

        ShoppingServerContext.Internals.setBaseDir(Paths.get(System.getProperty("jboss.server.base.dir")));
        ShoppingServerContext.Internals.setConfigDir(Paths.get(System.getProperty("jboss.server.config.dir")));
        ShoppingServerContext.Internals.setDataDir(Paths.get(System.getProperty("jboss.server.data.dir")));
        ShoppingServerContext.Internals.setLogDir(Paths.get(System.getProperty("jboss.server.log.dir")));
        ShoppingServerContext.Internals.setTempDir(Paths.get(System.getProperty("jboss.server.temp.dir")));

        ShoppingServerContext.Internals.setScheduledExecutor(scheduledExecutorDelegate);

        ThrowingRunnable ctx_close = ThrowingRunnable.noop();
        try {
            var ctx = new InitialContext();
            ctx_close = ctx::close;

            this.scheduledExecutorService = (ScheduledExecutorService) ctx
                    .lookup("java:jboss/ee/concurrency/scheduler/default");

            this.scheduledExecutorDelegate.setImpl(new ScheduledExecutorAdapter(this.scheduledExecutorService));

            this.defaultDataSource = (DataSource) ctx.lookup("java:jboss/datasources/shopping");
            ShoppingServerContext.Internals.setDefaultDataSource(defaultDataSource);

            prepareDatabase();

            initializeCodeServerReverseProxy();
        } catch (Exception caught) {
            this.context.log("Problems trying to initialize this context", caught);
        } finally {
            ctx_close.run();
        }

        this.context
                .addFilter(NoCacheFilter.class.getName(), new NoCacheFilter())
                .addMappingForUrlPatterns(null, true,
                        "/shopping/shopping.nocache.js",
                        "/dao/*",
                        "/index.html");

        {
            var r = this.context.addServlet(ProductImageServlet.class.getName(), new ProductImageServlet());
            r.setAsyncSupported(true);
            r.addMapping("/image/product/*");
        }

        {
            var r = this.context.addServlet(ShoppingDAOGwtServlet.class.getName(), new ShoppingDAOGwtServlet());
            r.setAsyncSupported(true);
            r.addMapping("/shopping/dao");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        this.scheduledExecutorDelegate.setImpl(null);
        this.scheduledExecutorService = null;
        this.defaultDataSource = null;
    }

    private void prepareDatabase() {
        try (var connection = this.defaultDataSource.getConnection()) {
            var cmd = new DBCreate().withConnection(connection);
            if ("true".equalsIgnoreCase(System.getProperty("wedocode.shopping.db.reset"))) {
                cmd.withReset();
            }
            cmd.run();
        } catch (final Exception caught) {
            Rethrow.emit(caught);
        }
    }

    private void initializeCodeServerReverseProxy() {
        // http://shopping.wedocode.devel:8080/shopping-gwt/shopping/shopping.nocache.js
        if ("true".equals(System.getProperty("gwt-use-reverse-proxy"))) {
            final var codeserverProxy = context.addServlet("codeserverProxy", new GwtProxyServlet());
            codeserverProxy.setInitParameter("module.name", "shopping");
            codeserverProxy.setInitParameter("targetUri", "http://127.0.0.1:9876");
            codeserverProxy.addMapping("/shopping/*");
        }
    }

}
