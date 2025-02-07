package br.com.wedocode.shopping.view.html.servlets;

import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutorDelegate;
import br.com.wedocode.framework.commons.function.ThrowingRunnable;
import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.Rethrow;
import br.com.wedocode.shopping.business.ScheduledExecutorAdapter;
import br.com.wedocode.shopping.business.ShoppingServerContext;
import br.com.wedocode.shopping.business.jdbc.ShoppingDAOJdbcImpl;
import br.com.wedocode.shopping.business.jdbc.util.DBCreate;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAODelegate;
import br.com.wedocode.shopping.view.html.servlets.viewimpl.ShoppingApplicationImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;

@WebListener
public class Activator implements ServletContextListener, HttpSessionActivationListener {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private ScheduledExecutorService scheduledExecutorService;
    private DataSource defaultDataSource;

    private ScheduledExecutorDelegate scheduledExecutorDelegate = new ScheduledExecutorDelegate(null);

    private ShoppingDAODelegate shoppingDAODelegate = new ShoppingDAODelegate();
    private ShoppingDAOJdbcImpl shoppingDAOJdbcImpl;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ShoppingServerContext.Internals.setBaseDir(Paths.get(System.getProperty("jboss.server.base.dir")));
        ShoppingServerContext.Internals.setConfigDir(Paths.get(System.getProperty("jboss.server.config.dir")));
        ShoppingServerContext.Internals.setDataDir(Paths.get(System.getProperty("jboss.server.data.dir")));
        ShoppingServerContext.Internals.setLogDir(Paths.get(System.getProperty("jboss.server.log.dir")));
        ShoppingServerContext.Internals.setTempDir(Paths.get(System.getProperty("jboss.server.temp.dir")));

        ShoppingContext.Internals.setExecutor(this.scheduledExecutorDelegate);
        ShoppingContext.Internals.setDAO(this.shoppingDAODelegate);

        ShoppingServerContext.Internals.setScheduledExecutor(this.scheduledExecutorDelegate);

        ThrowingRunnable ctx_close = ThrowingRunnable.noop();
        try {
            var ctx = new InitialContext();
            ctx_close = ctx::close;

            this.scheduledExecutorService = (ScheduledExecutorService) ctx
                    .lookup("java:jboss/ee/concurrency/scheduler/default");
            this.scheduledExecutorDelegate.setImpl(new ScheduledExecutorAdapter(this.scheduledExecutorService));

            this.defaultDataSource = (DataSource) ctx.lookup("java:jboss/datasources/shopping");
            ShoppingServerContext.Internals.setDefaultDataSource(this.defaultDataSource);
            prepareDatabase();

            this.shoppingDAOJdbcImpl = new ShoppingDAOJdbcImpl();
            this.shoppingDAODelegate.setImpl(this.shoppingDAOJdbcImpl);
        } catch (Exception caught) {
            event.getServletContext().log("Problems trying to initialize this context", caught);
        } finally {
            ctx_close.run();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.shoppingDAODelegate.setImpl(null);
        this.scheduledExecutorDelegate.setImpl(null);

        if (this.shoppingDAOJdbcImpl != null) {
            try {
                this.shoppingDAOJdbcImpl.close();
            } catch (Throwable caught) {
                LOG.error("Stopping shoppingDAOServerImpl", caught);
            } finally {
                this.shoppingDAOJdbcImpl = null;
            }
        }

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

    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        var httpSession = se.getSession();
        var app = ShoppingApplicationImpl.remove(httpSession);
        if (app != null) {
            httpSession.setAttribute("shopping.place", app.getFragment());
        }
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        try {
            var httpSession = se.getSession();
            var placeStr = CoerceUtils.toString(httpSession.getAttribute("shopping.place"));
            ShoppingApplicationImpl.getOrCreate(se.getSession(), placeStr);
        } catch (Exception caught) {
            LOG.error("Trying na restore app state", caught);
        }
    }

}
