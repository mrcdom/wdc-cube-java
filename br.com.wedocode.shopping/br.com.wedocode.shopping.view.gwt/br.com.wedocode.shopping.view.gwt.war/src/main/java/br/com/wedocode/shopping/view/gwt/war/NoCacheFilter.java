package br.com.wedocode.shopping.view.gwt.war;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoCacheFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(NoCacheFilter.class);

    private FilterConfig filterConfig;

    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }

    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        this.filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            if (response instanceof HttpServletResponse) {
                var httpresponse = (HttpServletResponse) response;
                // Set the Cache-Control and Expires header
                httpresponse.setHeader("Cache-Control", "no-cache");
                httpresponse.setHeader("Expires", "0");
            }
            chain.doFilter(request, response);
        } catch (IOException e) {
            LOG.error("IOException in NoCacheFilter", e);
        } catch (ServletException e) {
            LOG.error("ServletException in NoCacheFilter", e);
        }
    }
}