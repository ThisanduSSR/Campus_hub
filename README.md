# Campus Hub IoT Management API

A Java-based RESTful API for managing campus rooms and IoT sensors. This application is packaged as a WAR and designed to run in a servlet container such as Apache Tomcat.

## Overview

- API base path: `/api/v1`
- Technologies: Java 11, Maven, Jersey JAX-RS, Jackson JSON
- Data persistence: in-memory store using `ConcurrentHashMap`
- Deployment: WAR file named `ROOT.war` for root context deployment
- Error handling: custom exception mappers with structured JSON responses

## Project Structure

```
Campus_hub/
├── pom.xml
├── README.md
├── src/main/java/uk/ac/campus/
│   ├── boot/                    # JAX-RS application bootstrap
│   │   └── CampusHubApp.java
│   ├── api/                     # API resource controllers
│   │   ├── discovery/
│   │   ├── rooms/
│   │   └── sensors/
│   ├── domain/                  # Data models
│   ├── errors/                  # Custom exceptions and mappers
│   ├── middleware/              # Request audit filter
│   └── repository/              # In-memory data store
└── src/main/webapp/WEB-INF/
    └── web.xml                 # Jersey servlet configuration
```

## Prerequisites

- Java 11 or later
- Maven 3.6 or later
- Apache Tomcat 9+ (or another servlet container)

## Build

```bash
mvn clean package
```

This generates `target/ROOT.war`.

## Deployment

Copy `target/ROOT.war` into Tomcat's `webapps` directory and start Tomcat.

```bash
cp target/ROOT.war /path/to/tomcat/webapps/
```

Because the WAR is named `ROOT.war`, the API is available at:

```text
http://localhost:8080/api/v1/
```

If deployed under a different context, replace `/api/v1/` with the deployed context path.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/` | API discovery manifest |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get room details |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (returns `409` if sensors exist) |
| GET | `/api/v1/sensors` | List sensors; supports `?type=` filter |
| POST | `/api/v1/sensors` | Create a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get sensor details |
| PUT | `/api/v1/sensors/{sensorId}` | Update a sensor with payload ID validation |
| GET | `/api/v1/sensors/{sensorId}/readings` | List sensor readings |
| POST | `/api/v1/sensors/{sensorId}/readings` | Record a new sensor reading |

## Expected Behavior

- `PUT /api/v1/sensors/{sensorId}` validates that the path ID matches the payload `id`; mismatches return `400 Bad Request`.
- `DELETE /api/v1/rooms/{roomId}` returns `409 Conflict` if the room still has sensors.
- Custom errors are returned in JSON payloads with `httpCode`, `reason`, and `detail`.

## Sample Requests

### Discover API

```bash
curl -s http://localhost:8080/api/v1/ | jq
```

### Create a room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Reference Section","capacity":30}'
```

### Create a sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-LIB-301","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### Update a sensor

```bash
curl -X PUT http://localhost:8080/api/v1/sensors/TEMP-LIB-301 \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-LIB-301","type":"Temperature","status":"ACTIVE","roomId":"LIB-301"}'
```

### Submit a reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-LIB-301/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

### Delete a room with sensors (expect 409)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

## Notes

- This repo is ideal for demos, training, and API testing scenarios.
- The data store is non-persistent and resets when the application restarts.
- The servlet mapping is handled in `src/main/webapp/WEB-INF/web.xml`.
- The application registers JAX-RS resources from `uk.ac.campus.api`, error handlers from `uk.ac.campus.errors.handlers`, and middleware from `uk.ac.campus.middleware`.

## License

Add your preferred license information here.
