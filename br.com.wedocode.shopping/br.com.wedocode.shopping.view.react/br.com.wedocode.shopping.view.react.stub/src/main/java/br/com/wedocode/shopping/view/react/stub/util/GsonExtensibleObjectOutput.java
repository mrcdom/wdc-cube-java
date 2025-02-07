package br.com.wedocode.shopping.view.react.stub.util;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.gson.stream.JsonWriter;

import br.com.wedocode.framework.commons.serialization.ExtensibleObjectOutput;

public class GsonExtensibleObjectOutput implements ExtensibleObjectOutput, Closeable, Flushable {

    protected JsonWriter impl;
    protected boolean useIdAsKey;

    public GsonExtensibleObjectOutput(JsonWriter impl) {
        this.impl = impl;
    }

    public GsonExtensibleObjectOutput(JsonWriter impl, boolean useIdAsKey) {
        this.impl = impl;
        this.useIdAsKey = useIdAsKey;
    }

    @Override
    public void close() {
        try {
            this.impl.close();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    @Override
    public void flush() {
        try {
            this.impl.flush();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    public GsonExtensibleObjectOutput beginArray() {
        try {
            impl.beginArray();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput endArray() {
        try {
            impl.endArray();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput beginObject() {
        try {
            impl.beginObject();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput endObject() {
        try {
            impl.endObject();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput name(String name) {
        try {
            impl.name(name);
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput name(int id, String name) {
        try {
            if (this.useIdAsKey) {
                impl.name(String.valueOf(id));
            } else {
                impl.name(name);
            }
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput value(String value) {
        try {
            impl.value(value);
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput value(byte[] value) {
        try {
            if (value == null) {
                impl.nullValue();
                return this;
            }
            impl.value(Base64.getEncoder().encodeToString(value));
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput nullValue() {
        try {
            impl.nullValue();
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput value(boolean value) {
        try {
            impl.value(value);
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput value(double value) {
        try {
            impl.value(value);
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput value(long value) {
        try {
            impl.value(value);
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

    public GsonExtensibleObjectOutput value(Number value) {
        try {
            impl.value(value);
        } catch (IOException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        return this;
    }

}
