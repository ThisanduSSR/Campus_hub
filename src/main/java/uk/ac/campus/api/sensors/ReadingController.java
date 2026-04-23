package uk.ac.campus.api.sensors;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.domain.ReadingRecord;
import uk.ac.campus.domain.Sensor;
import uk.ac.campus.errors.exceptions.DeviceOfflineException;
import uk.ac.campus.repository.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 4 — Sensor Readings Sub-Resource Controller.
 *
 * This class is not annotated with @Path at the class level because it is
 * not registered directly with Jersey. Instead it is instantiated by
 * SensorController via the Sub-Resource Locator pattern — Jersey receives
 * it as the return value of a @Path-annotated method and delegates further
 * request processing to it. This pattern keeps large APIs modular by
 * distributing routing logic across focused classes rather than one
 * monolithic controller.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingController {

    private final String     sensorId;
    private final InMemoryStore store;

    public ReadingController(String sensorId, InMemoryStore store) {
        this.sensorId = sensorId;
        this.store    = store;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the complete telemetry history for the given sensor.
     */
    @GET
    public Response retrieveHistory() {
        List<ReadingRecord> history = store.getReadingsFor(sensorId);

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("sensorId", sensorId);
        envelope.put("count",    history.size());
        envelope.put("readings", history);

        return Response.ok(envelope).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     *
     * Appends a new telemetry measurement to the sensor's log.
     * Side effect (Part 4.2): updates Sensor.currentValue to keep it in sync
     * with the latest recorded measurement.
     *
     * Returns 403 if the sensor is under MAINTENANCE, 400 for invalid payload.
     */
    @POST
    public Response submitReading(ReadingRecord incoming) {
        if (incoming == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiErrorPayload(400, "Bad Request",
                            "Request body with a numeric 'value' field is required."))
                    .build();
        }

        // Retrieve parent sensor — existence already verified by SensorController locator
        Sensor parent = store.findSensor(sensorId).get();

        if (parent.isOfflineOrMaintenance()) {
            throw new DeviceOfflineException(sensorId);
        }

        // Build a proper record (generates UUID + timestamp)
        ReadingRecord persisted = new ReadingRecord(incoming.getValue());
        store.appendReading(sensorId, persisted);

        // Keep parent sensor's currentValue in sync with the latest reading
        parent.updateLatestReading(persisted.getValue());

        return Response
                .created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + persisted.getId()))
                .entity(persisted)
                .build();
    }
}
