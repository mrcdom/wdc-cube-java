package br.com.wedocode.shopping.view.gwt;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.view.gwt.shared.ShoppingDAOGwt;
import elemental2.dom.HTMLDivElement;

public class Main implements EntryPoint {

    ShoppingDAOGwtDelegate shoppingDAOGwtDelegate = new ShoppingDAOGwtDelegate();

    ShoppingApplicationGwt app;

    @Override
    public void onModuleLoad() {
        ShoppingContext.Internals.setExecutor(ScheduledExecutorGwt.get());
        ShoppingContext.Internals.setDAO(this.shoppingDAOGwtDelegate);
        this.shoppingDAOGwtDelegate.setImpl(GWT.create(ShoppingDAOGwt.class));

        this.app = new ShoppingApplicationGwt();
        this.app.setMainDiv((HTMLDivElement) document.getElementById("main"));
        window.onhashchange = this.app::onHashChanged;

        this.app.start();
    }

    protected void testDao() {
        var dao = ShoppingContext.getDAO();

        dao.loadSubject("admin", "admin")
                // OnSuccess
                .then(subject -> {
                    window.console.log(subject.getId() + " : " + subject.getNickName());
                    return null;
                })
                // OnFailure
                .catch_(caught -> {
                    window.console.log(caught.getMessage());
                    return null;
                });
    }

}
