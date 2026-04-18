package uk.ac.campus.errors.handlers;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.errors.exceptions.UnresolvableReferenceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps UnresolvableReferenceException → HTTP 422 Unprocessable Entity.
 * Used when a valid JSON request contains a reference field that points to
 * a non-existent resource (e.g. roomId that has not been created yet).
 */
@Provider
public class UnresolvableReferenceHandler implements ExceptionMapper<UnresolvableReferenceException> {

    @Override
    public Response toResponse(UnresolvableReferenceException ex) {
        return Response
                .status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorPayload(422, "Unprocessable Entity", ex.getMessage()))
                .build();
    }
}
