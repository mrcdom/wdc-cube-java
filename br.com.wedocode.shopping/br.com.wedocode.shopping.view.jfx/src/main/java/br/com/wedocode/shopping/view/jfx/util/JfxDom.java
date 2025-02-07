package br.com.wedocode.shopping.view.jfx.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;

public class JfxDom {

    private Parent currentParent;

    public static <T extends Parent> void render(T parent, BiConsumer<JfxDom, T> action) {
        JfxDom dom = new JfxDom();
        dom.currentParent = parent;
        try {
            action.accept(dom, parent);
        } finally {
            dom.currentParent = null;
        }
    }

    public ScrollPane scrollPane(Consumer<ScrollPane> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new ScrollPane();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }

    }

    public StackPane stackPane(Consumer<StackPane> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new StackPane();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public VBox vbox(Consumer<VBox> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new VBox();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public HBox hbox(Consumer<HBox> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new HBox();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public TextFlow textFlow(Consumer<TextFlow> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new TextFlow();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public FlowPane flowPane(Consumer<FlowPane> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new FlowPane();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public AnchorPane anchorPane(Consumer<AnchorPane> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new AnchorPane();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public GridPane gridPane(Consumer<GridPane> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new GridPane();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public BorderPane borderPane(Consumer<BorderPane> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new BorderPane();
            this.currentParent = elm;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public ImageView img(Consumer<ImageView> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new ImageView();
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Text text(Consumer<Text> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new Text();
            elm.getStyleClass().setAll("text");
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Label label(Consumer<Label> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new Label();
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public TextField textField(Consumer<TextField> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new TextField();
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public PasswordField passwordField(Consumer<PasswordField> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new PasswordField();
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Button button(Consumer<Button> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new Button();
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public WebView webView(Consumer<WebView> fnUpdate) {
        var oldParent = this.currentParent;
        try {
            var elm = new WebView();
            this.currentParent = null;
            fnUpdate.accept(elm);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Region hSpacer() {
        var oldParent = this.currentParent;
        try {
            var elm = new Region();
            this.currentParent = null;
            HBox.setHgrow(elm, Priority.ALWAYS);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Region hSpacer(int width) {
        var oldParent = this.currentParent;
        try {
            var elm = new Region();
            this.currentParent = null;
            elm.setMinWidth(width);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Region vSpacer() {
        var oldParent = this.currentParent;
        try {
            var elm = new Region();
            this.currentParent = null;
            VBox.setVgrow(elm, Priority.ALWAYS);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    public Region vSpacer(int height) {
        var oldParent = this.currentParent;
        try {
            var elm = new Region();
            this.currentParent = null;
            elm.setMinHeight(height);
            getChildren(oldParent).add(elm);
            return elm;
        } finally {
            this.currentParent = oldParent;
        }
    }

    // :: Internal

    private ObservableList<Node> getChildren(Parent parent) {
        if (parent instanceof Pane) {
            return ((Pane) parent).getChildren();
        } else if (parent instanceof Group) {
            return ((Group) parent).getChildren();
        } else {
            throw new RuntimeException("Not supported parent type. Just Pane and Group are supported");
        }
    }

}
