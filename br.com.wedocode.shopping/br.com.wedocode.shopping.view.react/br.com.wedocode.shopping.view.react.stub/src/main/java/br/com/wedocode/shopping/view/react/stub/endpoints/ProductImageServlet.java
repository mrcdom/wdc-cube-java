package br.com.wedocode.shopping.view.react.stub.endpoints;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.wedocode.shopping.business.jdbc.commands.LoadProductImageCommand;
import br.com.wedocode.shopping.presentation.shared.business.exception.DAOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/image/product/*")
public class ProductImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOG = LoggerFactory.getLogger(ProductImageServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long productId;
        try {
            productId = parseProductId(req);
        } catch (Exception e) {
            LOG.error("Parsing productId from URL", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        byte[] imageBytes;
        try {
            imageBytes = LoadProductImageCommand.loadProductImage(productId);
        } catch (DAOException caught) {
            LOG.error("Processing request", caught);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (imageBytes == null) {
            resp.sendError(HttpServletResponse.SC_NO_CONTENT);
        } else {
            resp.setContentType("image/png");
            resp.getOutputStream().write(imageBytes);
        }
    }

    private Long parseProductId(HttpServletRequest req) throws Exception {
        var uri = req.getRequestURI() == null ? "" : req.getRequestURI();
        if (uri.endsWith(".png")) {
            var idx = uri.lastIndexOf('/');
            return Long.parseLong(uri.substring(idx + 1, uri.length() - 4));
        } else {
            throw new IOException("Missing productId on URL");
        }
    }

}
