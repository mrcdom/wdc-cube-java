package br.com.wedocode.shopping.view.html.servlets.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class FormData {

    private String eventId;

    private Map<String, String[]> params;

    public FormData(Map<String, String[]> params) {
        this.params = params;
        this.eventId = this.getString("event");
    }

    public String getEventId() {
        return this.eventId;
    }

    public String getString(String paramName, String defaultValue) {
        var paramValue = this.params.get(paramName);
        if (paramValue != null && paramValue.length > 0) {
            return StringUtils.isNotBlank(paramValue[0]) ? paramValue[0] : defaultValue;
        }
        return defaultValue;
    }

    public String getString(String paramName) {
        return this.getString(paramName, null);
    }

    public Integer getInteger(String paramName, Integer defaultValue) {
        var paramVales = this.params.get(paramName);
        if (paramVales != null && paramVales.length > 0) {
            try {
                return Integer.parseInt(paramVales[0]);
            } catch (final NumberFormatException exn) {
                // NOOP
            }
        }
        return defaultValue;
    }

    public Integer getInteger(String paramName) {
        return this.getInteger(paramName, null);
    }

    public Long getLong(String paramName, Long defaultValue) {
        var paramVales = this.params.get(paramName);
        if (paramVales != null && paramVales.length > 0) {
            try {
                return Long.parseLong(paramVales[0]);
            } catch (final NumberFormatException exn) {
                // NOOP;
            }
        }
        return defaultValue;
    }

    public Long getLong(String paramName) {
        return this.getLong(paramName, null);
    }

}
