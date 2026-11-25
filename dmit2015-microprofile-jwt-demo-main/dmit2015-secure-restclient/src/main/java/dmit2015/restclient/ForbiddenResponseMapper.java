package dmit2015.restclient;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

public class ForbiddenResponseMapper
        implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(Response response) {

        return new WebApplicationException(
                "Access denied. You do not have permission "
                        + "to perform this operation.",
                Response.Status.FORBIDDEN);
    }

    @Override
    public boolean handles(
            int status,
            MultivaluedMap<String, Object> headers) {

        return status == 403;
    }
}