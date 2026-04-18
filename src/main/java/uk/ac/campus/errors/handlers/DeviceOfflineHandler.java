package uk.ac.campus.errors.handlers;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.errors.exceptions.DeviceOfflineException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps DeviceOfflineException → HTTP 403 Forbidden.
 * Fired when a telemetry POST targets a sensor that is under MAINTENANCE.
 */
@Provider
public class DeviceOfflineHandler implements ExceptionMapper<DeviceOfflineException> {

    @Override
    public Response toResponse(DeviceOfflineException ex) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorPayload(403, "Forbidden", ex.getMessage()))
                .build();
    }
}
