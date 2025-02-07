package br.com.wedocode.shopping.view.react.stub.endpoints;

import java.io.IOException;
import java.security.SecureRandom;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import br.com.wedocode.framework.commons.util.Base62;
import br.com.wedocode.shopping.view.react.stub.util.AppSecurity;

@WebFilter(urlPatterns = "/index.html")
public class WdcAppIdFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        if (resp instanceof HttpServletResponse httpResp) {
            var security = AppSecurity.BEAN;

            var appIdCookie = new Cookie("app_id", makeAppId());
            appIdCookie.setMaxAge(10);
            httpResp.addCookie(appIdCookie);

            var pubKeyCookie = new Cookie("app_skey", security.getWebKey());
            pubKeyCookie.setMaxAge(-1);
            httpResp.addCookie(pubKeyCookie);
        }

        chain.doFilter(req, resp);
    }

    private String makeAppId() {
        var security = AppSecurity.BEAN;
        var b62 = Base62.BEAN;

        var appIdPart1 = (String) null;
        {
            var rnd = new SecureRandom();
            var appIdPart1Bytes = new byte[32];
            rnd.nextBytes(appIdPart1Bytes);
            appIdPart1 = b62.encodeToString(appIdPart1Bytes);
        }

        var appIdPart2 = b62.encodeToString(security.signAsHash(appIdPart1.getBytes()));

        return appIdPart1 + "." + appIdPart2;
    }

}
