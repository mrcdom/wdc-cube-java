package br.com.wedocode.shopping.view.jfx.util;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.shopping.business.jdbc.commands.LoadProductImageCommand;
import br.com.wedocode.shopping.view.jfx.impl.LoginViewJfx;
import javafx.scene.image.Image;

public class ResourceCatalog {

    private static Logger LOG = LoggerFactory.getLogger(ResourceCatalog.class);

    private static Map<String, Image> imageMap;

    private static Image noImageFound;

    static {
        imageMap = new HashMap<>();
        putImage("images/big_logo.png");
        putImage("images/logo.png");
        putImage("images/carrinho.png");
        putImage("images/delet.png");
        noImageFound = putImage("images/no-image-found.png");
    }

    private static Image putImage(String uri) {
        var image = new Image(LoginViewJfx.class.getResourceAsStream("/META-INF/resources/" + uri));
        imageMap.put(uri, image);
        return image;
    }

    public static Image getImage(String resourceId) {
        var image = imageMap.get(resourceId);
        if (image == null) {
            image = getImageProductImage(resourceId);
        }

        if (image == null) {
            image = noImageFound;
        }

        return image;
    }

    private static Image getImageProductImage(String url) {
        if (url != null && url.startsWith("image/product/") && url.endsWith(".png")) {
            var beforePngIdx = url.indexOf(".png");
            if (beforePngIdx == -1) {
                return null;
            }

            try {

                var lastSlashIdx = url.lastIndexOf('/');
                var productId = Long.parseLong(url.substring(lastSlashIdx + 1, beforePngIdx));
                var bytes = LoadProductImageCommand.loadProductImage(productId);
                return new Image(new ByteArrayInputStream(bytes));
            } catch (Exception caught) {
                LOG.error("Loading product image based on PATH " + url, caught);
            }
        }
        return null;
    }

}
