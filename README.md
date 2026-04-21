# Campus Hub IoT Management API

A RESTful API built with JAX-RS (Jersey) and an embedded Grizzly HTTP server for managing campus rooms and IoT sensor devices. No external servlet container or database is required — all data is held in-memory using `ConcurrentHashMap`.

---

## Project Structure

```
campus-hub-api/
├── pom.xml
└── src/main/java/uk/ac/campus/
    ├── boot/                        # Server bootstrap
    │   ├── CampusHubApp.java        # JAX-RS Application (@ApplicationPath)
    │   └── ServerLauncher.java      # Main entry point (Grizzly server)
    ├── domain/                      # Plain data models (POJOs)
    │   ├── Room.java
    │   ├── Sensor.java
    │   ├── ReadingRecord.java       # Equivalent to SensorReading
    │   └── ApiErrorPayload.java     # Uniform error response envelope
    ├── repository/
    │   └── InMemoryStore.java       # Singleton ConcurrentHashMap data store
    ├── api/                         # JAX-RS resource controllers
    │   ├── discovery/DiscoveryController.java
    │   ├── rooms/RoomController.java
    │   └── sensors/
    │       ├── SensorController.java
    │       └── ReadingController.java   # Sub-resource (not path-registered)
    ├── errors/
    │   ├── exceptions/              # Custom runtime exceptions
    │   │   ├── EntityNotFoundException.java
    │   │   ├── RoomOccupiedException.java
    │   │   ├── UnresolvableReferenceException.java
    │   │   └── DeviceOfflineException.java
    │   └── handlers/                # JAX-RS ExceptionMappers
    │       ├── EntityNotFoundHandler.java
    │       ├── RoomOccupiedHandler.java
    │       ├── UnresolvableReferenceHandler.java
    │       ├── DeviceOfflineHandler.java
    │       └── GlobalFaultBarrier.java
    └── middleware/
        └── RequestAuditFilter.java  # Request/response logging filter
```

---

## Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Build
```bash
mvn clean package
```
This produces a fat JAR at `target/campus-hub-api-2.0.0.jar` with all dependencies bundled.

### Run
```bash
java -jar target/campus-hub-api-2.0.0.jar
```
The API starts at `http://localhost:8080/api/v1`.

### Alternatively (without packaging)
```bash
mvn exec:java
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1` | Discovery — API manifest with HATEOAS links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Register a new room |
| GET | `/api/v1/rooms/{roomId}` | Fetch a room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Decommission a room (blocked if sensors present) |
| GET | `/api/v1/sensors` | List sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Fetch a sensor by ID |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get telemetry history |
| POST | `/api/v1/sensors/{sensorId}/readings` | Submit a new reading |

---

## Sample `curl` Commands

### 1. Discover the API
```bash
curl -s http://localhost:8080/api/v1 | python3 -m json.tool
```

### 2. List all rooms
```bash
curl -s http://localhost:8080/api/v1/rooms
```

### 3. Create a new room
```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CONF-401","name":"Innovation Hub","capacity":20}'
```

### 4. Register a new sensor
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-042","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"CONF-401"}'
```

### 5. Submit a sensor reading
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-042/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

### 6. Filter sensors by type
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 7. Attempt to delete a room that has sensors (expect 409)
```bash
curl -s -X DELETE http://localhost:8080/api/v1/sensors/ENG-201
```

### 8. Get reading history for a sensor
```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-042/readings
```

---

## Conceptual Report (Question Answers)

### Part 1.1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a **new instance** of every resource class for each incoming HTTP request (per-request scope). This means fields are never shared between requests via the resource object itself. The consequence for in-memory data management is that state cannot be stored as instance fields on the resource class — if it were, data would be lost the moment the request completed and the instance was garbage-collected.

This project addresses that by using a **singleton** (`InMemoryStore`) held outside the resource class. All per-request instances call `InMemoryStore.getInstance()`, which returns the same shared object. `ConcurrentHashMap` is used throughout the store so that concurrent requests (running on different threads with different resource instances) can read and write safely without explicit `synchronized` blocks or locks.

### Part 1.2 — Why HATEOAS Is a Hallmark of Advanced REST

HATEOAS (Hypermedia As The Engine Of Application State) embeds navigable links directly inside API responses, much like anchor tags in HTML. Instead of consulting static documentation to learn that rooms can be managed at `/api/v1/rooms`, a client can read the discovery response and follow the provided `href`. This reduces coupling between client and server: if a URL structure changes, only the server needs updating — clients that follow links rather than hard-coding paths continue to work. It also accelerates onboarding because the API is self-describing and explorable at runtime.

### Part 2.1 — Returning Full Objects vs IDs

Returning only IDs forces each client to issue N additional requests to fetch the details of each room (N+1 problem), multiplying network round-trips and latency. Returning full objects sends more data per response but eliminates those follow-up calls, which is usually the right trade-off for collections of modest size. For very large collections, pagination with full objects or a `fields` projection parameter is a better approach than ID-only lists.

### Part 2.2 — Is DELETE Idempotent?

In this implementation, DELETE returns **404** if the room does not exist. Strictly speaking, a truly idempotent DELETE would return **204** even on a second call (same server state — the resource is absent either way). The choice to return 404 is a deliberate UX trade-off: it informs the client that the resource was already absent, which is more useful than a silent 204. Many production APIs (including GitHub's) take this approach. The underlying system state is still idempotent — calling DELETE ten times leaves the room absent just as calling it once does.

### Part 3.1 — Effect of `@Consumes(APPLICATION_JSON)` Mismatch

`@Consumes(MediaType.APPLICATION_JSON)` declares that the annotated method only accepts requests whose `Content-Type` header is `application/json`. If a client submits `text/plain` or `application/xml`, the JAX-RS runtime itself rejects the request with **HTTP 415 Unsupported Media Type** before the method body is invoked. No application code runs. This is a clean contract enforcement mechanism — the resource method is guaranteed to receive a properly typed and deserialised entity, or not be called at all.

### Part 3.2 — `@QueryParam` vs Path Segment for Filtering

Query parameters (`?type=CO2`) are semantically designed for filtering, searching, and sorting — they narrow a collection without identifying a specific resource. A path segment (`/sensors/type/CO2`) implies that `type/CO2` is itself a resource with a stable identity, which is semantically inaccurate for a filter. Query parameters are also inherently optional, keeping the base URL (`/sensors`) clean and meaningful on its own. They compose naturally: `?type=CO2&status=ACTIVE` is straightforward, whereas nesting multiple filter segments into a path quickly becomes unwieldy and non-standard.

### Part 4.1 — Sub-Resource Locator Architectural Benefits

The Sub-Resource Locator pattern allows a large API to be split across multiple focused controller classes. `SensorController` handles sensor CRUD; `ReadingController` handles all reading operations. Without this pattern, every sensor *and* reading endpoint would be jammed into one class, making it long, hard to test, and difficult to reason about. The locator method also serves as a natural place to validate parent-resource existence before delegating — if the sensor ID is invalid, a 404 is thrown before `ReadingController` is ever instantiated. This mirrors good object-oriented design: each class has a single, clear responsibility.

### Part 5.2 — Why 422 Is More Accurate Than 404

**404 Not Found** means the URL itself could not be resolved — the path identifies nothing. **422 Unprocessable Entity** means the URL was valid and the server understood the request, but the *content* of the body is semantically incorrect. When a client POSTs a sensor with a `roomId` that does not exist, the endpoint `/api/v1/sensors` is perfectly valid (404 would be wrong). The problem is that a field *inside* the payload references a resource that cannot be located. 422 precisely communicates "your JSON is well-formed but contains a reference that cannot be satisfied", which gives the client actionable information: fix the `roomId`, not the URL.

### Part 5.4 — Security Risk of Exposing Stack Traces

A Java stack trace leaks several categories of sensitive information:
- **Package and class names** — reveals the internal architecture, framework versions, and naming conventions an attacker can target.
- **File paths and line numbers** — pinpoints exactly where errors originate, helping an attacker craft inputs that trigger specific code paths.
- **Library versions** — allows an attacker to cross-reference known CVEs for the exact dependency versions in use.
- **Business logic clues** — method names and parameter types can reveal how data is processed, stored, or validated.

The `GlobalFaultBarrier` catches all unhandled `Throwable` instances, logs full detail server-side (where only authorised staff can see it), and returns a generic 500 message to the client — eliminating all of the above leakage vectors.

### Part 5.5 — Filters vs Inline Logger Calls

Placing a `Logger.info()` call inside every resource method violates the **Don't Repeat Yourself** principle and the **Single Responsibility Principle**. If the log format needs changing, every method must be edited. If a new endpoint is added and the developer forgets to add logging, that endpoint is silently unobserved. A JAX-RS filter is a **cross-cutting concern** — it intercepts every request and response automatically, regardless of which resource class handles it. Adding a new endpoint requires zero logging code; the filter covers it by design. This is the same reason servlet filters, Spring interceptors, and middleware pipelines exist in other frameworks.
