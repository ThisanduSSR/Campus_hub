package uk.ac.campus.errors.handlers;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.errors.exceptions.EntityNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps EntityNotFoundException → HTTP 404 Not Found.
 */
@Provider
public class EntityNotFoundHandler implements ExceptionMapper<EntityNotFoundException> {

    @Override
    public Response toResponse(EntityNotFoundException ex) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorPayload(404, "Not Found", ex.getMessage()))
                .build();
    }
}
