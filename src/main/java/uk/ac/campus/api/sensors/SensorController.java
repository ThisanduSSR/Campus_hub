package uk.ac.campus.api.sensors;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.domain.Sensor;
import uk.ac.campus.errors.exceptions.EntityNotFoundException;
import uk.ac.campus.errors.exceptions.UnresolvableReferenceException;
import uk.ac.campus.repository.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor Operations Controller.
 * Handles all HTTP operations on the /api/v1/sensors collection.
 *
 * Also acts as the parent locator for the ReadingController sub-resource
 * at /api/v1/sensors/{sensorId}/readings (Part 4.1).
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorController {

    private final InMemoryStore store = InMemoryStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Lists all registered sensors. Supports an optional ?type= query parameter
     * for filtering by sensor category (e.g. ?type=CO2).
     *
     * Using @QueryParam is preferable to embedding the filter in the path
     * (e.g. /sensors/type/CO2) because query parameters are semantically
     * intended for filtering, searching, and pagination of collections,
     * whereas path segments identify specific resource instances.
     * Query parameters are also optional by default, keeping the base URL clean.
     */
    @GET
    public Response listSensors(@QueryParam("type") String typeFilter) {
        List<Sensor> all = new ArrayList<>(store.sensors().values());

        List<Sensor> result = (typeFilter == null || typeFilter.isBlank())
                ? all
                : all.stream()
                     .filter(s -> s.getType().equalsIgnoreCase(typeFilter))
                     .collect(Collectors.toList());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("count",   result.size());
        envelope.put("filter",  typeFilter != null ? typeFilter : "none");
        envelope.put("sensors", result);

        return Response.ok(envelope).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor device. Validates that the referenced roomId
     * actually exists before persisting — returns 422 if it does not.
     *
     * @Consumes(APPLICATION_JSON) tells JAX-RS that this method only handles
     * requests with Content-Type: application/json. If a client sends
     * text/plain or application/xml, the JAX-RS runtime rejects the request
     * with 415 Unsupported Media Type before the method body is ever invoked.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor incoming) {
        if (incoming == null || isBlank(incoming.getId())) {
            return badRequest("Field 'id' is required.");
        }
        if (isBlank(incoming.getType())) {
            return badRequest("Field 'type' is required (e.g. Temperature, CO2, Occupancy).");
        }
        if (isBlank(incoming.getRoomId())) {
            return badRequest("Field 'roomId' is required.");
        }

        // 422: roomId is syntactically valid JSON but references a non-existent room
        if (!store.roomExists(incoming.getRoomId())) {
            throw new UnresolvableReferenceException("roomId", incoming.getRoomId());
        }

        if (store.sensorExists(incoming.getId())) {
            return conflict("A sensor with ID '" + incoming.getId() + "' is already registered.");
        }

        // Default status to ACTIVE when omitted
        if (isBlank(incoming.getStatus())) {
            incoming.setStatus(Sensor.DeviceStatus.ACTIVE.name());
        }

        store.saveSensor(incoming);

        // Keep the parent room's device list consistent
        store.findRoom(incoming.getRoomId())
             .ifPresent(room -> room.attachDevice(incoming.getId()));

        return Response
                .created(URI.create("/api/v1/sensors/" + incoming.getId()))
                .entity(incoming)
                .build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a single sensor by ID, or 404 if not found.
     */
    @GET
    @Path("/{sensorId}")
    public Response fetchSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.findSensor(sensorId)
                .orElseThrow(() -> new EntityNotFoundException("Sensor", sensorId));
        return Response.ok(sensor).build();
    }

    /**
     * Part 4.1 — Sub-Resource Locator for sensor readings.
     *
     * This method carries no HTTP-method annotation. Jersey recognises it as a
     * locator because it returns an object instance rather than a Response.
     * Jersey then inspects that returned object for @GET / @POST annotations
     * to continue dispatching the request. This cleanly delegates all reading
     * logic to ReadingController, keeping each class focused on one concern.
     */
    @Path("/{sensorId}/readings")
    public ReadingController locateReadingController(@PathParam("sensorId") String sensorId) {
        // Validate parent existence before handing off to the sub-resource
        if (!store.sensorExists(sensorId)) {
            throw new EntityNotFoundException("Sensor", sensorId);
        }
        return new ReadingController(sensorId, store);
    }

    // ---- helpers ------------------------------------------------------------

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Response badRequest(String detail) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorPayload(400, "Bad Request", detail))
                .build();
    }

    private Response conflict(String detail) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorPayload(409, "Conflict", detail))
                .build();
    }
}
