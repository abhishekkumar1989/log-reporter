package pw.server.logreporter.api.filter;

import pw.server.logreporter.api.servlet.StatusExposingServletResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StatusReportingFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        StatusExposingServletResponse statusExposingServletResponse = new StatusExposingServletResponse((HttpServletResponse) response);
        chain.doFilter(request, statusExposingServletResponse);
    }

    @Override
    public void destroy() {

    }
}
