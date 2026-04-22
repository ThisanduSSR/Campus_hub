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
