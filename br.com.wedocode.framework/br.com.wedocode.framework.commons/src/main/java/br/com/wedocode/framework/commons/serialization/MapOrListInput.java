package br.com.wedocode.framework.commons.serialization;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.wedocode.framework.commons.util.CoerceUtils;

@SuppressWarnings({"rawtypes"})
public class MapOrListInput implements ExtensibleObjectInput {

    private static Double ZERO_DOUBLE = 0.0;
    private static Integer ZERO_INTEGER = 0;
    private static Long ZERO_LONG = 0L;

    private StackItem current;

    public MapOrListInput(Map map) {
        this.current = new StackItem();
        this.current.name = null;
        this.current.value = map;
        this.current.token = SerializationToken.BEGIN_OBJECT;
    }

    public MapOrListInput(List list) {
        this.current = new StackItem();
        this.current.name = null;
        this.current.value = list;
        this.current.token = SerializationToken.BEGIN_ARRAY;
    }

    @Override
    public void beginObject() throws IOException {
        if (this.current.token != SerializationToken.BEGIN_OBJECT) {
            throw new IOException("Expected BEGIN_OBJECT but found " + this.current.token.name());
        }
        this.current.token = SerializationToken.END_OBJECT;

        if (this.current.value instanceof Map) {
            StackItem stackItem = new StackItem();
            stackItem.privious = this.current;
            stackItem.name = stackItem.name;
            stackItem.value = stackItem.value;
            stackItem.it = ((Map) this.current.value).entrySet().iterator();
            stackItem.hasValue = false;
            this.current = stackItem;
            this.fetchNext();
        } else {
            throw new IOException("Expected value as a Map object");
        }
    }

    @Override
    public void endObject() throws IOException {
        StackItem previousStackItem = this.current.privious;
        if (previousStackItem == null) {
            throw new IOException("Expected an element but no one was found");
        }

        if (previousStackItem.token != SerializationToken.END_OBJECT) {
            throw new IOException("Expected END_OBJECT but found " + this.current.token.name());
        }

        this.current = previousStackItem;
        if (this.current.privious != null) {
            this.fetchNext();
        }
    }

    @Override
    public void beginArray() throws IOException {
        if (this.current.token != SerializationToken.BEGIN_ARRAY) {
            throw new IOException("Expected BEGIN_ARRAY but found " + this.current.token.name());
        }
        this.current.token = SerializationToken.END_ARRAY;

        if (this.current.value instanceof List) {
            StackItem stackItem = new StackItem();
            stackItem.privious = this.current;
            stackItem.name = stackItem.name;
            stackItem.value = stackItem.value;
            stackItem.it = ((List) this.current.value).iterator();
            stackItem.hasValue = false;
            this.current = stackItem;
            this.fetchNext();
        } else {
            throw new IOException("Expected value as a List object");
        }
    }

    @Override
    public void endArray() throws IOException {
        StackItem previousStackItem = this.current.privious;
        if (previousStackItem == null) {
            throw new IOException("Expected an element but no one was found");
        }

        if (previousStackItem.token != SerializationToken.END_ARRAY) {
            throw new IOException("Expected END_ARRAY but found " + this.current.token.name());
        }

        this.current = previousStackItem;
        if (this.current.privious != null) {
            this.fetchNext();
        }
    }

    private void fetchNext() throws IOException {
        if (this.current.it != null) {
            if (this.current.it.hasNext()) {
                Object item = this.current.it.next();
                if (item instanceof Map.Entry) {
                    Map.Entry entry = (Map.Entry) item;
                    this.current.name = CoerceUtils.toString(entry.getKey());
                    item = entry.getValue();
                }

                this.current.value = item;
                if (this.current.value == null) {
                    this.current.token = SerializationToken.NULL;
                } else if (this.current.value instanceof Map) {
                    this.current.token = SerializationToken.BEGIN_OBJECT;
                } else if (this.current.value instanceof List) {
                    this.current.token = SerializationToken.BEGIN_ARRAY;
                } else if (this.current.value instanceof String) {
                    this.current.token = SerializationToken.STRING;
                } else if (this.current.value instanceof Character) {
                    this.current.token = SerializationToken.STRING;
                } else if (this.current.value instanceof Number) {
                    this.current.token = SerializationToken.NUMBER;
                } else if (this.current.value instanceof Boolean) {
                    this.current.token = SerializationToken.BOOLEAN;
                } else {
                    throw new IOException("Non supported value: " + this.current.value);
                }
                this.current.hasValue = true;
            } else {
                this.current.name = null;
                this.current.value = null;
                this.current.hasValue = false;
                if (this.current.privious != null) {
                    this.current.token = this.current.privious.token;
                }
            }
        } else {
            throw new IOException("Expected an iterator but found " + this.current.value);
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return this.current.hasValue;
    }

    @Override
    public SerializationToken peek() throws IOException {
        return this.current.token;
    }

    @Override
    public String nextName() throws IOException {
        return this.current.name;
    }

    @Override
    public <T> T nextNull() throws IOException {
        if (this.current.value != null) {
            throw new IOException("Expected null value but found " + this.current.value);
        }
        this.fetchNext();
        return null;
    }

    @Override
    public String nextString() throws IOException {
        String result = CoerceUtils.toString(this.current.value, null);
        this.fetchNext();
        return result;
    }

    @Override
    public boolean nextBoolean() throws IOException {
        boolean result = CoerceUtils.toBoolean(this.current.value, Boolean.FALSE);
        this.fetchNext();
        return result;
    }

    @Override
    public Number nextNumber() throws IOException {
        Number result = CoerceUtils.toNumber(this.current.value, null);
        this.fetchNext();
        return result;
    }

    @Override
    public double nextDouble() throws IOException {
        double result = CoerceUtils.toDouble(this.current.value, ZERO_DOUBLE);
        this.fetchNext();
        return result;
    }

    @Override
    public long nextLong() throws IOException {
        long result = CoerceUtils.toLong(this.current.value, ZERO_LONG);
        this.fetchNext();
        return result;
    }

    @Override
    public int nextInt() throws IOException {
        int result = CoerceUtils.toInteger(this.current.value, ZERO_INTEGER);
        this.fetchNext();
        return result;
    }

    @Override
    public void skipValue() throws IOException {
        this.current.value = null;
        this.fetchNext();
    }

    private static class StackItem {
        StackItem privious;

        SerializationToken token;

        String name;
        Object value;

        Iterator it;

        boolean hasValue;
    }

}
