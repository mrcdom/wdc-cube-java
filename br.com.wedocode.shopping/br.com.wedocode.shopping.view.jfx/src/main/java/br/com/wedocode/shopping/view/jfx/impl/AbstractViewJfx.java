package br.com.wedocode.shopping.view.jfx.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import br.com.wedocode.framework.commons.util.Reference;
import br.com.wedocode.framework.webflow.WebFlowPresenter;
import br.com.wedocode.framework.webflow.WebFlowView;
import br.com.wedocode.shopping.view.jfx.ShoppingJfxApplication;
import br.com.wedocode.shopping.view.jfx.util.JfxUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public abstract class AbstractViewJfx<P extends WebFlowPresenter> implements WebFlowView {

    public static int viewIdGen = 0;

    private static Predicate<? super Node> TRUE_PREDICATE = o -> Boolean.TRUE;

    /*
     * Fields
     */

    protected final String id;

    protected final ShoppingJfxApplication app;

    protected final P presenter;

    protected boolean released;

    private Runnable fnDoUpdate;

    protected Parent element;

    /*
     * Constructor
     */

    public AbstractViewJfx(String id, ShoppingJfxApplication app, P presenter, Parent element) {
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

            if (this.element != this.app.getRootElement()) {
                var parentElement = this.element.getParent();
                if (parentElement instanceof Pane) {
                    ((Pane) parentElement).getChildren().remove(this.element);
                }
            }
            this.released = true;
        }
    }

    public Parent getElement() {
        return this.element;
    }

    @Override
    public void update() {
        this.app.pushUpdate(this.id, this.fnDoUpdate);
    }

    protected abstract void doUpdate();

    protected static Consumer<Node> newOneSlot(Parent contentSlotElm) {
        var children = JfxUtil.getChildren(contentSlotElm);
        var oldContentElmRef = new Reference<Node>();

        return newContentElm -> {
            var oldContentElm = oldContentElmRef.get();

            if (oldContentElm != newContentElm) {
                children.removeIf(TRUE_PREDICATE);

                JfxUtil.removeFromParent(oldContentElm);

                if (newContentElm != null && newContentElm.getParent() != contentSlotElm) {
                    children.add(newContentElm);
                }

                oldContentElmRef.set(newContentElm);
            }
        };
    }

    protected static <V extends AbstractViewJfx<?>, D> BiConsumer<List<D>, List<V>> newListSlot(Parent contentSlotElm,
            Supplier<V> fnNewView, BiConsumer<V, D> fnUpdate) {
        var children = JfxUtil.getChildren(contentSlotElm);

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
                    children.add(item.element);
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
