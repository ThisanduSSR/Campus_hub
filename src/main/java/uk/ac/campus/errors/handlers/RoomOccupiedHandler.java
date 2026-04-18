package uk.ac.campus.errors.handlers;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.errors.exceptions.RoomOccupiedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps RoomOccupiedException → HTTP 409 Conflict.
 * Triggered when a DELETE is attempted on a room that still contains sensors.
 */
@Provider
public class RoomOccupiedHandler implements ExceptionMapper<RoomOccupiedException> {

    @Override
    public Response toResponse(RoomOccupiedException ex) {
        return Response
                .status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorPayload(409, "Conflict", ex.getMessage()))
                .build();
    }
}
