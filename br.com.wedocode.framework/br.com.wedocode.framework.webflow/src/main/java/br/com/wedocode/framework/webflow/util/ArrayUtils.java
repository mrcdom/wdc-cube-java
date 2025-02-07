package br.com.wedocode.framework.webflow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ArrayUtils {

    private static Map<Class<?>, Function<Integer, Object>> arrayFactoryMap;

    static {
        arrayFactoryMap = new HashMap<>();
        arrayFactoryMap.put(String.class, len -> new String[len]);
        arrayFactoryMap.put(byte.class, len -> new byte[len]);
        arrayFactoryMap.put(short.class, len -> new short[len]);
        arrayFactoryMap.put(char.class, len -> new char[len]);
        arrayFactoryMap.put(int.class, len -> new int[len]);
        arrayFactoryMap.put(long.class, len -> new long[len]);
        arrayFactoryMap.put(float.class, len -> new float[len]);
        arrayFactoryMap.put(double.class, len -> new double[len]);
    }

    public static Object newInstance(Class<?> componentType, int length) {
        var factory = arrayFactoryMap.get(componentType);
        if (factory != null) {
            return factory.apply(length);
        } else {
            return new Object[length];
        }
    }

}
