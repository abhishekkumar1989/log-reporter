package pw.server.logreporter.api.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class StatusExposingServletResponse extends HttpServletResponseWrapper {

    private int httpStatus = 200;

    public StatusExposingServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void sendError(int sc) throws IOException {
        httpStatus = sc;
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        httpStatus = sc;
        super.sendError(sc, msg);
    }

    @Override
    public void setStatus(int sc) {
        httpStatus = sc;
        super.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        httpStatus = sc;
        super.setStatus(sc, sm);
    }


    @Override
    public void sendRedirect(String location) throws IOException {
        httpStatus = 302;
        super.sendRedirect(location);
    }

    public int getStatus() {
        return httpStatus;
    }
}
