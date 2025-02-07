package br.com.wedocode.shopping.view.jfx.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class JfxUtil {

    public static void removeFromParent(Node node) {
        if (node != null && node.getParent() != null) {
            JfxUtil.getChildren(node.getParent()).remove(node);
        }
    }

    public static ObservableList<Node> getChildren(Node node) {
        if (node instanceof Pane) {
            return ((Pane) node).getChildren();
        } else if (node instanceof Group) {
            return ((Group) node).getChildren();
        } else {
            return FXCollections.emptyObservableList();
        }
    }

}
