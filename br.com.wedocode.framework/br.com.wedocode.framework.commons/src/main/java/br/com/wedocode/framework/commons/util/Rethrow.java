package br.com.wedocode.framework.commons.util;

public class Rethrow {

    @SuppressWarnings("unchecked")
    public static <R, E extends Exception> R emit(Throwable ex) throws E {
        throw (E) ex;
    }

}
