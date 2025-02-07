package br.com.wedocode.shopping.view.react.stub.endpoints;

import java.net.HttpCookie;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class WdcStateDispatcherConfiguratator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        var cookieHeaderVals = request.getHeaders().get("Cookie");
        if (cookieHeaderVals != null && cookieHeaderVals.size() > 0) {
            for (var cookieStr : cookieHeaderVals) {
                for (var cookie : HttpCookie.parse(cookieStr)) {
                    if ("app_signature".equals(cookie.getName())) {
                        config.getUserProperties().put("APP_SIGNATURE", cookie.getValue());
                        continue;
                    }
                }
            }
        }
    }
}
