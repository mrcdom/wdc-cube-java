package br.com.wedocode.framework.commons.util;

import java.lang.reflect.Array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoerceUtils {

    private static Logger LOG = LoggerFactory.getLogger(CoerceUtils.class);

    public static String toString(final Object value) {
        return CoerceUtils.toString(value, null);
    }

    public static String toString(final Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value.getClass().isArray()) {
            Object firstValue = Array.get(value, 0);
            return CoerceUtils.toString(firstValue);
        }

        return value.toString();
    }

    public static Boolean toBoolean(final Object value) {
        return CoerceUtils.toBoolean(value, null);
    }

    public static Boolean toBoolean(final Object value, final Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }

        if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value);
        }

        if (value.getClass().isArray()) {
            Object firstValue = Array.get(value, 0);
            return CoerceUtils.toBoolean(firstValue, defaultValue);
        }

        return defaultValue;
    }

    public static Integer toInteger(final Object value) {
        return CoerceUtils.toInteger(value, null);
    }

    public static Integer toInteger(final Object value, final Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exn) {
            LOG.warn(exn.getMessage(), exn);
            return defaultValue;
        }
    }

    public static Long toLong(final Object value) {
        return CoerceUtils.toLong(value, null);
    }

    public static Long toLong(final Object value, final Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exn) {
            LOG.warn(exn.getMessage(), exn);
            return defaultValue;
        }
    }

    public static Float toFloat(final Object value) {
        return CoerceUtils.toFloat(value, null);
    }

    public static Float toFloat(final Object value, final Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException exn) {
            LOG.warn(exn.getMessage(), exn);
            return defaultValue;
        }
    }

    public static Double toDouble(final Object value) {
        return CoerceUtils.toDouble(value, null);
    }

    public static Double toDouble(final Object value, final Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException exn) {
            LOG.warn(exn.getMessage(), exn);
            return defaultValue;
        }
    }

    public static Number toNumber(final Object value) {
        return CoerceUtils.toNumber(value, null);
    }

    public static Number toNumber(final Object value, final Number defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return (Number) value;
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }

        if (value instanceof String) {
            try {
                String s = (String) value;
                if (s.indexOf('.') != -1) {
                    return Double.parseDouble((String) value);
                } else {
                    return Long.parseLong(s);
                }
            } catch (NumberFormatException exn) {
                LOG.warn(exn.getMessage(), exn);
                return defaultValue;
            }
        }

        if (value.getClass().isArray()) {
            Object firstValue = Array.get(value, 0);
            return CoerceUtils.toNumber(firstValue, defaultValue);
        }

        try {
            String s = value.toString();
            if (s.indexOf('.') != -1) {
                return Double.parseDouble((String) value);
            } else {
                return Long.parseLong(s);
            }
        } catch (NumberFormatException exn) {
            LOG.warn(exn.getMessage(), exn);
            return defaultValue;
        }
    }

}
