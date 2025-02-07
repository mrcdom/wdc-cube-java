package br.com.wedocode.shopping.view.gwt.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLParagraphElement;

public class HtmlDom {

    private volatile HTMLElement currentParent;

    public static <T extends HTMLElement> void render(T parent, BiConsumer<HtmlDom, T> action) {
        var dom = new HtmlDom();
        dom.currentParent = parent;
        try {
            action.accept(dom, parent);
        } finally {
            dom.currentParent = null;
        }
    }

    public HTMLDivElement div(Consumer<HTMLDivElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newDiv();
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLImageElement img(Consumer<HTMLImageElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newImage();
            this.currentParent = null;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLParagraphElement paragraph(Consumer<HTMLParagraphElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newParagraph();
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLInputElement input(Consumer<HTMLInputElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newInput();
            this.currentParent = null;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLButtonElement button(Consumer<HTMLButtonElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newButton();
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLAnchorElement anchor(Consumer<HTMLAnchorElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newAnchor();
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLElement header(int sz, Consumer<HTMLElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newHSpan(sz);
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLElement span(Consumer<HTMLElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newSpan();
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLElement br() {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newBreak();
            this.currentParent = null;
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HTMLElement bold(Consumer<HTMLElement> update) {
        var oldParent = this.currentParent;
        try {
            var elm = DomFactory.newBoldSpan();
            this.currentParent = elm;
            update.accept(elm);
            oldParent.appendChild(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

}
