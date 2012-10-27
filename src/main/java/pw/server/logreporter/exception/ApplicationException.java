package pw.server.logreporter.exception;

import java.io.Serializable;

public class ApplicationException extends RuntimeException implements Serializable{

    private int errorCode;

    public ApplicationException(int errorCode) {
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ApplicationException(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
