package br.com.wedocode.shopping.view.gwt.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import br.com.wedocode.framework.commons.util.Reference;
import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.view.gwt.ShoppingApplicationGwt;
import br.com.wedocode.shopping.view.gwt.util.DomFactory;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

public abstract class AbstractViewGwt<P extends WebFlowPresenter> implements WebFlowView {

    public static int viewIdGen = 0;

    /*
     * Fields
     */

    protected final String id;

    protected final ShoppingApplicationGwt app;

    protected final P presenter;

    protected boolean released;

    private Runnable fnDoUpdate;

    protected HTMLDivElement element;

    /*
     * Constructor
     */

    public AbstractViewGwt(String id, ShoppingApplicationGwt app, P presenter) {
        this(id, app, presenter, DomFactory.newDiv());
    }

    public AbstractViewGwt(String id, ShoppingApplicationGwt app, P presenter, HTMLDivElement element) {
        this.id = id;
        this.app = app;
        this.presenter = presenter;
        this.element = element;
        this.fnDoUpdate = this::doUpdate;
        this.update();
    }

    /*
     * API
     */

    @Override
    public void release() {
        if (!this.released) {
            this.app.removeUpdate(this.id);

            if (this.element != this.app.getMainDiv()) {
                var parentElement = this.element.parentElement;
                if (parentElement != null) {
                    parentElement.removeChild(this.element);
                }
            }
            this.released = true;
        }
    }

    public HTMLDivElement getElement() {
        return this.element;
    }

    @Override
    public void update() {
        this.app.pushUpdate(this.id, this.fnDoUpdate);
    }

    protected abstract void doUpdate();

    protected static Consumer<HTMLElement> newOneSlot(HTMLElement contentSlotElm) {
        var oldContentElmRef = new Reference<HTMLElement>();

        return newContentElm -> {
            var oldContentElm = oldContentElmRef.get();

            if (oldContentElm != newContentElm) {
                var child = contentSlotElm.firstChild;
                while (child != null) {
                    contentSlotElm.removeChild(child);
                    child = child.nextSibling;
                }

                if (oldContentElm != null && oldContentElm.parentElement != null) {
                    oldContentElm.parentElement.removeChild(oldContentElm);
                }

                if (newContentElm != null && newContentElm.parentElement != contentSlotElm) {
                    contentSlotElm.appendChild(newContentElm);
                }

                oldContentElmRef.set(newContentElm);
            }
        };
    }

    protected static <V extends AbstractViewGwt<?>, D> BiConsumer<List<D>, List<V>> newListSlot(
            HTMLElement contentSlotElm, Supplier<V> fnNewView, BiConsumer<V, D> fnUpdate) {
        return (dataList, viewList) -> {
            var nonNullDataList = Optional.ofNullable(dataList).orElseGet(Collections::emptyList);

            var i = 0;
            for (var len = Math.min(nonNullDataList.size(), viewList.size()); i < len; i++) {
                var item = viewList.get(i);
                fnUpdate.accept(item, nonNullDataList.get(i));
                item.doUpdate();
            }

            for (; i < nonNullDataList.size(); i++) {
                if (i >= viewList.size()) {
                    var item = fnNewView.get();
                    fnUpdate.accept(item, nonNullDataList.get(i));
                    contentSlotElm.appendChild(item.element);
                    viewList.add(item);
                } else {
                    var item = viewList.get(i);
                    fnUpdate.accept(item, nonNullDataList.get(i));
                    item.doUpdate();
                }
            }

            for (var j = viewList.size() - 1; j >= i; j--) {
                var item = viewList.remove(j);
                item.release();
            }
        };
    }

}
