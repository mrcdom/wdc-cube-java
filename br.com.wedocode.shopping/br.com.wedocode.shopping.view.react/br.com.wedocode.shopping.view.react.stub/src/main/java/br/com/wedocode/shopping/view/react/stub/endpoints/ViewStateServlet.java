package br.com.wedocode.shopping.view.react.stub.endpoints;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import br.com.wedocode.shopping.view.react.stub.util.GsonExtensibleObjectOutput;
import br.com.wedocode.shopping.view.react.stub.viewimpl.ApplicationReactImpl;

@WebServlet(urlPatterns = "/view-state")
public class ViewStateServlet extends HttpServlet {

    private static final long serialVersionUID = 587107276976310887L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var appId = req.getHeader("X-Application-Id");
        if (StringUtils.isBlank(appId)) {
            resp.getWriter().print("[]");
            return;
        }

        var app = ApplicationReactImpl.get(appId);
        if (app == null) {
            resp.getWriter().print("[]");
            return;
        }

        String[] vsids = new Gson().fromJson(req.getReader(), String[].class);
        if (vsids == null) {
            resp.getWriter().print("[]");
            return;
        }

        if (vsids.length == 1 && "all".equals(vsids[0])) {
            var viewMap = app.getViewMap();
            vsids = new ArrayList<>(viewMap.keySet()).toArray(n -> new String[n]);
        }

        writeResponse(resp, app, vsids);
    }

    private void writeResponse(HttpServletResponse resp, ApplicationReactImpl app, String[] vsids)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (var out = resp.getWriter(); var json = new GsonExtensibleObjectOutput(new JsonWriter(out))) {
            json.beginArray();
            for (var vsid : vsids) {
                var view = app.getViewInstanceById(vsid);
                if (view != null) {
                    view.writeState(json);
                }
            }
            json.endArray();
            json.flush();
        }
    }

}
