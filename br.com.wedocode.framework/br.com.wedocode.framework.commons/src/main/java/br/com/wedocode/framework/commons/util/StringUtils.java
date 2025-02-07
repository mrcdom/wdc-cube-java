package br.com.wedocode.framework.commons.util;

public class StringUtils {

    protected StringUtils() {

    }

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

}
