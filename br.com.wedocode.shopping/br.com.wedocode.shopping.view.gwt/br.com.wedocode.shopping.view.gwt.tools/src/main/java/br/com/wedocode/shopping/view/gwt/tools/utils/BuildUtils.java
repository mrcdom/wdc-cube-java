package br.com.wedocode.shopping.view.gwt.tools.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class BuildUtils {

    public static File getJarFileFromRepo(final Class<?> cls) {
        try {
            return new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (final URISyntaxException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static File getServerWarFolder() {
        var dir = new File(BuildUtils.getJarFileFromRepo(br.com.wedocode.shopping.view.gwt.Main.class),
                "../../../br.com.wedocode.shopping.view.gwt.war");
        try {
            return dir.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
