package br.com.wedocode.framework.commons.util;

public class Reference<V> {

    private V value;

    public Reference() {
        // NOOP
    }

    public Reference(V value) {
        this.value = value;
    }

    public V get() {
        return value;
    }

    public void set(V value) {
        this.value = value;
    }

}
