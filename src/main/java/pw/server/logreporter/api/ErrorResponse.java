package pw.server.logreporter.api;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import pw.server.logreporter.exception.ApplicationException;

import java.io.IOException;
import java.io.Serializable;

public class ErrorResponse implements Serializable {

    private ApplicationException exception;

    public ErrorResponse(ApplicationException exception) {
        this.exception = exception;
    }

    public String getError() {
        return exception.getMessage();
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {}
        throw new RuntimeException("An error occurred while converting to Json");
    }

}

