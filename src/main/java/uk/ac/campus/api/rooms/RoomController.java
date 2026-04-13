package uk.ac.campus.api.rooms;

import uk.ac.campus.domain.ApiErrorPayload;
import uk.ac.campus.domain.Room;
import uk.ac.campus.errors.exceptions.EntityNotFoundException;
import uk.ac.campus.errors.exceptions.RoomOccupiedException;
import uk.ac.campus.repository.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 2 — Room Management Controller.
 * Handles all HTTP operations on the /api/v1/rooms collection.
 *
 * JAX-RS creates a new instance of this class for every incoming request
 * (per-request lifecycle). The InMemoryStore singleton is fetched via
 * getInstance() each time, ensuring all request-scoped instances share
 * the same underlying data without synchronisation issues on the store
 * reference itself.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomController {

    private final InMemoryStore store = InMemoryStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns every registered room wrapped in an envelope with a count.
     * Returning full objects avoids a second round-trip for the client,
     * whereas returning only IDs would force N+1 requests to hydrate details.
     */
    @GET
    public Response listRooms() {
        List<Room> roomList = new ArrayList<>(store.rooms().values());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("count", roomList.size());
        envelope.put("rooms", roomList);

        return Response.ok(envelope).build();
    }

    /**
     * POST /api/v1/rooms
     * Registers a new room. Returns 201 Created on success, 400 for invalid
     * input, or 409 if the room ID already exists.
     */
    @POST
    public Response registerRoom(Room incoming) {

        if (incoming == null || isBlank(incoming.getId())) {
            return badRequest("Field 'id' is required and must not be blank.");
        }
        if (isBlank(incoming.getName())) {
            return badRequest("Field 'name' is required and must not be blank.");
        }
        if (incoming.getCapacity() <= 0) {
            return badRequest("Field 'capacity' must be a positive integer.");
        }
        if (store.roomExists(incoming.getId())) {
            return conflict("A room with ID '" + incoming.getId() + "' is already registered.");
        }

        // Ensure the sensor list is always initialised on creation
        if (incoming.getSensorIds() == null) {
            incoming.setSensorIds(new ArrayList<>());
        }

        store.saveRoom(incoming);

        return Response
                .created(URI.create("/api/v1/rooms/" + incoming.getId()))
                .entity(incoming)
                .build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Fetches a single room by its identifier. Returns 404 if not found.
     */
    @GET
    @Path("/{roomId}")
    public Response fetchRoom(@PathParam("roomId") String roomId) {
        Room room = store.findRoom(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room", roomId));
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     *
     * Decommissions a room. Blocked with 409 if the room still has sensors.
     *
     * Idempotency note: A truly idempotent DELETE would return 204 even when
     * the resource is already absent (second call produces same outcome).
     * This implementation returns 404 on the second call, which is a deliberate
     * trade-off: it provides more informative feedback to clients at the cost
     * of strict idempotency. Many real-world APIs take this approach.
     */
    @DELETE
    @Path("/{roomId}")
    public Response decommissionRoom(@PathParam("roomId") String roomId) {
        Room room = store.findRoom(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room", roomId));

        if (room.hasDevices()) {
            throw new RoomOccupiedException(roomId, room.getSensorIds().size());
        }

        store.removeRoom(roomId);
        return Response.noContent().build();
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
