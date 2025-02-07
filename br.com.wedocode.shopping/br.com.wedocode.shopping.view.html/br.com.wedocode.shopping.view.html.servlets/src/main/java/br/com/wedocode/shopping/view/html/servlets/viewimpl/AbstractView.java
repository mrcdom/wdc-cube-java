package br.com.wedocode.shopping.view.html.servlets.viewimpl;

import java.io.PrintWriter;

import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.view.html.servlets.util.FormData;

public abstract class AbstractView implements WebFlowView {

    @Override
    public void release() {

    }

    @Override
    public void update() {

    }

    public abstract void syncState(FormData form, Promise<Boolean> actions);

    public abstract void render(PrintWriter wr);

    public void syncStateChild(WebFlowView childView, FormData form, Promise<Boolean> actions) {
        if (childView instanceof AbstractView) {
            ((AbstractView) childView).syncState(form, actions);
        }
    }

    public boolean renderChild(WebFlowView childView, PrintWriter wr) {
        if (childView instanceof AbstractView) {
            ((AbstractView) childView).render(wr);
            return true;
        } else {
            return false;
        }
    }

}
