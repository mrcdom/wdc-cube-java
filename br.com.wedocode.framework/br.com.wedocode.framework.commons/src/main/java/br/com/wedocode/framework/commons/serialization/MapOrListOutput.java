package br.com.wedocode.framework.commons.serialization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MapOrListOutput implements ExtensibleObjectOutput {

    private StackItem<?> current;

    public MapOrListOutput() {
        StackItem<Object> item = new StackItem<>();
        item.add = (v) -> item.value = v;
        this.current = item;
    }

    public MapOrListOutput(Map<?, ?> mapOfAnyTypeOfKeyOrValue) {
        StackItem<Map<String, Object>> item = new StackItem<>();
        item.value = anyCast(mapOfAnyTypeOfKeyOrValue);
        item.add = newAddOnMap(item, item.value);
        this.current = item;
    }

    public MapOrListOutput(List<?> listOfAnyTypeOfValue) {
        StackItem<List<Object>> item = new StackItem<>();
        item.value = anyCast(listOfAnyTypeOfValue);
        item.add = newAddOnList(item.value);
        this.current = item;
    }

    public Object getValue() {
        return this.current.value;
    }

    @Override
    public ExtensibleObjectOutput beginArray() {
        StackItem<List<Object>> item = new StackItem<>();
        item.privious = this.current;
        item.value = new ArrayList<>();
        item.add = newAddOnList(item.value);
        this.current = item;
        return this;
    }

    @Override
    public ExtensibleObjectOutput endArray() {
        this.current.privious.add.accept(this.current.value);
        this.current = this.current.privious;
        return this;
    }

    @Override
    public ExtensibleObjectOutput beginObject() {
        StackItem<Map<String, Object>> item = new StackItem<>();
        item.privious = this.current;
        item.value = new LinkedHashMap<>();
        item.add = newAddOnMap(item, item.value);
        this.current = item;
        return this;
    }

    @Override
    public ExtensibleObjectOutput endObject() {
        this.current.privious.add.accept(this.current.value);
        this.current = this.current.privious;
        return this;
    }

    @Override
    public ExtensibleObjectOutput name(String name) {
        this.current.name = name;
        return this;
    }

    @Override
    public ExtensibleObjectOutput name(int id, String name) {
        this.current.name = name == null || name.isBlank() ? String.valueOf(id) : name;
        return this;
    }

    @Override
    public ExtensibleObjectOutput value(String value) {
        this.current.add.accept(value);
        return this;
    }

    @Override
    public ExtensibleObjectOutput value(byte[] value) {
        this.current.add.accept(value);
        return this;
    }

    @Override
    public ExtensibleObjectOutput nullValue() {
        this.current.add.accept(null);
        return this;
    }

    @Override
    public ExtensibleObjectOutput value(boolean value) {
        this.current.add.accept(value);
        return this;
    }

    @Override
    public ExtensibleObjectOutput value(double value) {
        this.current.add.accept(value);
        return this;
    }

    @Override
    public ExtensibleObjectOutput value(long value) {
        this.current.add.accept(value);
        return this;
    }

    @Override
    public ExtensibleObjectOutput value(Number value) {
        this.current.add.accept(value);
        return this;
    }

    // :: Helpers

    @SuppressWarnings("unchecked")
    private static <T> T anyCast(Object v) {
        return (T) v;
    }

    private static Consumer<Object> newAddOnList(List<Object> list) {
        return (obj) -> list.add(obj);
    }

    private static <T> Consumer<Object> newAddOnMap(StackItem<T> current, Map<String, Object> map) {
        return (obj) -> map.put(current.name, (Object) obj);
    }

    private static class StackItem<T> {
        StackItem<?> privious;

        String name;
        T value;

        Consumer<Object> add;
    }

}
