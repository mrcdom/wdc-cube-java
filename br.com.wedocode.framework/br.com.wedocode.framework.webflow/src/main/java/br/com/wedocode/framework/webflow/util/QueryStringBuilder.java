package br.com.wedocode.framework.webflow.util;

import java.lang.reflect.Array;
import java.util.Map;

import br.com.wedocode.framework.commons.util.CoerceUtils;
import br.com.wedocode.framework.commons.util.StringUtils;

/**
 * Helps to build a query string
 */
public class QueryStringBuilder {

    StringBuilder query = new StringBuilder();

    public void appendValue(final String name, final Object value) {
        if (value != null) {
            var svalue = CoerceUtils.toString(value);
            if (StringUtils.isNotBlank(svalue)) {
                if (this.query.length() > 0) {
                    this.query.append('&');
                }
                this.query.append(name);
                this.query.append('=');

                this.query.append(svalue);
            }

        }
    }

    public QueryStringBuilder append(final Map<String, Object> parameters) {
        for (var entry : parameters.entrySet()) {
            var value = entry.getValue();
            if (value == null) {
                continue;
            }

            String name = entry.getKey();

            if (value.getClass().isArray()) {
                for (int i = 0, iSize = Array.getLength(value); i < iSize; i++) {
                    this.appendValue(name, Array.get(value, i));
                }
            } else {
                this.appendValue(name, value);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return this.query.toString();
    }

}