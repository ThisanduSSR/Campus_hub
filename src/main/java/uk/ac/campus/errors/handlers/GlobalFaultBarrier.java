package uk.ac.campus.errors.handlers;

import uk.ac.campus.domain.ApiErrorPayload;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global fault barrier — intercepts any Throwable not caught by a more
 * specific mapper. Logs full detail server-side while returning a safe,
 * opaque 500 error to the caller. This prevents internal implementation
 * details (stack traces, class names, memory addresses) from leaking
 * to external clients, which is critical from a security standpoint.
 */
@Provider
public class GlobalFaultBarrier implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalFaultBarrier.class.getName());

    @Override
    public Response toResponse(Throwable thrown) {
        if (thrown instanceof WebApplicationException) {
            return ((WebApplicationException) thrown).getResponse();
        }

        LOG.log(Level.SEVERE, "Unhandled fault intercepted by global barrier", thrown);

        ApiErrorPayload payload = new ApiErrorPayload(
                500,
                "Internal Server Error",
                "An unexpected fault occurred on the server. Please contact support if this persists."
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload)
                .build();
    }
}
