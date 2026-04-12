package uk.ac.campus.api.discovery;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 1.2 — API Discovery Endpoint.
 *
 * GET /api/v1 returns an API manifest with version metadata, admin contact,
 * and HATEOAS-style links to all primary resource collections.
 *
 * HATEOAS (Hypermedia As The Engine Of Application State) is considered a
 * hallmark of mature REST design because it makes the API self-describing.
 * Clients receive navigable links alongside data, meaning they do not need
 * to hard-code URLs or rely solely on external documentation to discover
 * capabilities — they can follow links dynamically, much like a browser
 * follows href attributes on web pages.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryController {

    private static final String API_VERSION  = "2.0.0";
    private static final String BASE_PATH    = "/api/v1";

    @GET
    public Response manifest() {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("service",     "Campus Hub IoT Management API");
        body.put("version",     API_VERSION);
        body.put("description", "Centralised RESTful API for managing campus rooms and IoT sensor devices.");
        body.put("generatedAt", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        body.put("contact",     buildContact());
        body.put("endpoints",   buildEndpointIndex());

        return Response.ok(body).build();
    }

    private Map<String, String> buildContact() {
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team",       "Campus Infrastructure Team");
        contact.put("email",      "infra@campus.ac.uk");
        contact.put("department", "Estates, Facilities & Digital Services");
        return contact;
    }

    private List<Map<String, Object>> buildEndpointIndex() {
        List<Map<String, Object>> endpoints = new ArrayList<>();

        endpoints.add(entry("rooms",            "GET, POST",   BASE_PATH + "/rooms",
                "Retrieve all rooms or register a new room"));
        endpoints.add(entry("room-detail",      "GET, DELETE", BASE_PATH + "/rooms/{roomId}",
                "Fetch or decommission a specific room by ID"));
        endpoints.add(entry("sensors",          "GET, POST",   BASE_PATH + "/sensors",
                "List sensors (supports ?type= filter) or register a new device"));
        endpoints.add(entry("sensor-detail",    "GET",         BASE_PATH + "/sensors/{sensorId}",
                "Retrieve a specific sensor by ID"));
        endpoints.add(entry("sensor-readings",  "GET, POST",   BASE_PATH + "/sensors/{sensorId}/readings",
                "Retrieve historical readings or submit a new measurement for a sensor"));

        return endpoints;
    }

    private Map<String, Object> entry(String rel, String methods, String href, String description) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("rel",         rel);
        e.put("href",        href);
        e.put("methods",     methods);
        e.put("description", description);
        return e;
    }
}
