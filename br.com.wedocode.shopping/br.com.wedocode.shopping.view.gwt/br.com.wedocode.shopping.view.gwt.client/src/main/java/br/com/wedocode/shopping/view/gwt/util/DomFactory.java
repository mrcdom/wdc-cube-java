package br.com.wedocode.shopping.view.gwt.util;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLBRElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLParagraphElement;

public class DomFactory {

    public static HTMLDivElement newDiv() {
        return (HTMLDivElement) DomGlobal.document.createElement("div");
    }

    public static HTMLImageElement newImage() {
        return (HTMLImageElement) DomGlobal.document.createElement("img");
    }

    public static HTMLParagraphElement newParagraph() {
        return (HTMLParagraphElement) DomGlobal.document.createElement("p");
    }

    public static HTMLInputElement newInput() {
        return (HTMLInputElement) DomGlobal.document.createElement("input");
    }

    public static HTMLButtonElement newButton() {
        return (HTMLButtonElement) DomGlobal.document.createElement("button");
    }

    public static HTMLAnchorElement newAnchor() {
        return (HTMLAnchorElement) DomGlobal.document.createElement("a");
    }

    public static HTMLElement newHSpan(int sz) {
        return (HTMLElement) DomGlobal.document.createElement("h" + sz);
    }

    public static HTMLElement newSpan() {
        return (HTMLElement) DomGlobal.document.createElement("span");
    }

    public static HTMLBRElement newBreak() {
        return (HTMLBRElement) DomGlobal.document.createElement("br");
    }

    public static HTMLElement newBoldSpan() {
        return (HTMLElement) DomGlobal.document.createElement("b");
    }
}
