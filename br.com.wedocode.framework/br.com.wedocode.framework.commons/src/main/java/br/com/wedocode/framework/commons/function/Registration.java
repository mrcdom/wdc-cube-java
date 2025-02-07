package br.com.wedocode.framework.commons.function;

public interface Registration {

    Registration NOOP = () -> {
        // NOOP
    };

    void remove();

}
