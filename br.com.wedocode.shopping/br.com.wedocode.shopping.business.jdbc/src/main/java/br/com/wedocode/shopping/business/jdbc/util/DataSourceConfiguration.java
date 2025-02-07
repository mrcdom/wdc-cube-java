package br.com.wedocode.shopping.business.jdbc.util;

public class DataSourceConfiguration {

    private String driverClassName;
    private Integer initialSize;
    private Integer maxActive;
    private Integer maxIdle;
    private String password;
    private Boolean reset;
    private String url;
    private String userName;
    private String validationQuery;

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public Integer getInitialSize() {
        return this.initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMaxActive() {
        return this.maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMaxIdle() {
        return this.maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getReset() {
        return this.reset;
    }

    public boolean isReset() {
        return Boolean.TRUE.equals(this.reset);
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getValidationQuery() {
        return this.validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

}
