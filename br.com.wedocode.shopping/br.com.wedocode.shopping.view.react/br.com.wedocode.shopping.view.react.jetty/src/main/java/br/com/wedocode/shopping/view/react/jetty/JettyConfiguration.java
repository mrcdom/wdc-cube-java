package br.com.wedocode.shopping.view.react.jetty;

public class JettyConfiguration {

    private Integer maxThreads;
    private Integer minThreads;
    private Integer threadTimeoutMillis;
    private String host;
    private Integer port;
    private Long idleTimeout;
    private String shoppingDataSource;

    public Integer getMaxThreads() {
        return this.maxThreads;
    }

    public void setMaxThreads(Integer maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Integer getMinThreads() {
        return this.minThreads;
    }

    public void setMinThreads(Integer minThreads) {
        this.minThreads = minThreads;
    }

    public Integer getThreadTimeoutMillis() {
        return this.threadTimeoutMillis;
    }

    public void setThreadTimeoutMillis(Integer threadTimeoutMillis) {
        this.threadTimeoutMillis = threadTimeoutMillis;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getIdleTimeout() {
        return this.idleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getShoppingDataSource() {
        return this.shoppingDataSource;
    }

    public void setShoppingDataSource(String shoppingDataSource) {
        this.shoppingDataSource = shoppingDataSource;
    }

}
