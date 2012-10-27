package pw.server.logreporter.api;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pw.server.logreporter.exception.ApplicationException;
import pw.server.logreporter.util.NullChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Service
public class BaseController {

    @ExceptionHandler(ApplicationException.class)
    public void applicationExceptionHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, ApplicationException e) {
        try {
            if (NullChecker.isNotNull(e.getCause())) {
                Logger.getLogger(getClass()).error(e.getMessage(), e.getCause());
            } else {
                Logger.getLogger(getClass()).info("An error occurred while handling the response: " + e.getMessage());
            }
            ErrorResponse errorResponse = new ErrorResponse(e);
            response.getWriter().write(errorResponse.toJson());
            response.setStatus(mapToHttpCode(e.getErrorCode()));
            response.setContentType(MediaType.APPLICATION_JSON.toString());
        } catch (IOException ioe) {
            Logger.getLogger(getClass()).error("Unable to process error", ioe);
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public void missingServletParameterExceptionHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, MissingServletRequestParameterException e) {
        ApplicationException applicationException = new ApplicationException(e.getMessage(), ErrorCodes.SC_BAD_REQUEST);
        applicationExceptionHandler(request, response, session, applicationException);
    }

    @ExceptionHandler(Exception.class)
    public void exceptionHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception e) {
        ApplicationException applicationException = new ApplicationException("An internal error has occurred", e, ErrorCodes.SC_INTERNAL_SERVER_ERROR);
        applicationExceptionHandler(request, response, session, applicationException);
    }

    private int mapToHttpCode(int errorCode) {
        return errorCode;
    }

}
