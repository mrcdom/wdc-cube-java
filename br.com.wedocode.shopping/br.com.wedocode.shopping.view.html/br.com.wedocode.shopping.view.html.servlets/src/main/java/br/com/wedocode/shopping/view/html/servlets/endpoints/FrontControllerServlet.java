package br.com.wedocode.shopping.view.html.servlets.endpoints;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.framework.commons.function.ThrowingConsumer;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.commons.util.Reference;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;
import br.com.wedocode.shopping.view.html.servlets.viewimpl.ShoppingApplicationImpl;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, urlPatterns = "/do")
public class FrontControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOG = LoggerFactory.getLogger(FrontControllerServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return System.currentTimeMillis();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var resolveRef = new Reference<ThrowingConsumer<Boolean>>();

        final var action = new Promise<Boolean>((resolve, reject) -> resolveRef.set(resolve));

        // Try to get asynchronous context
        AsyncContext aContext;
        try {
            aContext = req.startAsync(req, resp);
        } catch (Throwable caught) {
            LOG.error("Processing request", caught);
            resp.sendRedirect("error.html");
            return;
        }

        try {
            doPostAsync(action, aContext);
        } catch (Throwable caught) {
            action.then(dummy -> Promise.reject(caught));
        } finally {
            action.catch_(caught -> {
                LOG.error("Processing request", caught);
                resp.sendRedirect("error.html");
                return null;
            });

            action.finally_(() -> {
                aContext.complete();
            });
        }

        // Start processing request
        resolveRef.get().accept(Boolean.TRUE);
    }

    protected void doPostAsync(Promise<Boolean> action, AsyncContext aContext) throws ServletException, IOException {
        var req = (HttpServletRequest) aContext.getRequest();
        var resp = (HttpServletResponse) aContext.getResponse();

        final var form = new FormData(req.getParameterMap());
        final var session = req.getSession(true);

        var place = form.getString("place");
        var app = ShoppingApplicationImpl.getOrCreate(session, place);

        synchronized (app) {
            app.setHttpSession(session);

            if (!Objects.equals(place, app.getFragment())) {
                action.then(app.go(place));
            }

            app.syncState(form, action);

            action

                    // Last action
                    .then(result -> {
                        try {
                            synchronized (app) {
                                app.commitComputedState();

                                if (app.isHistoryDirty()) {
                                    app.doUpdateHistory();
                                }
                            }
                        } catch (Throwable caught) {
                            // This is an internal error. User does not need to know
                            LOG.error("Trying to stablish state consitency", caught);
                        }

                        return Promise.resolve(result);
                    })

                    // On any unhandled failure
                    .catch_(caught -> {
                        synchronized (app) {
                            app.alertUnexpectedError(LOG, "Unhandled exception", caught);
                        }
                        return null;
                    })

                    // Lastly, return response
                    .finally_(() -> {
                        resp.setCharacterEncoding("UTF-8");

                        synchronized (app) {
                            app.render(resp.getWriter());
                        }
                    });
        }
    }

}
