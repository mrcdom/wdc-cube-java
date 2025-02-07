package br.com.wedocode.shopping.view.gwt.war;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

public class GwtProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {

    private static final long serialVersionUID = 1L;

    private String modulePath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String moduleName = config.getInitParameter("module.name");
        if (moduleName == null || (moduleName = moduleName.trim()).isEmpty()) {
            moduleName = config.getServletContext().getServletContextName();
        }

        modulePath = "/" + moduleName;
    }

    @Override
    protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
        if (servletRequest.getPathInfo() == null) {
            return modulePath;
        } else {
            return modulePath + servletRequest.getPathInfo();
        }
    }

}
