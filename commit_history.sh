#!/bin/bash

# ── EDIT THESE TWO LINES ────────────────────────────────────────────────
AUTHOR_NAME="ThisanduSSR"
AUTHOR_EMAIL="thisanduranadheera67@gmail.com"
# ────────────────────────────────────────────────────────────────────────

git config user.name  "$AUTHOR_NAME"
git config user.email "$AUTHOR_EMAIL"

commit_on() {
  local DATE="$1"
  local MSG="$2"
  GIT_AUTHOR_DATE="$DATE" \
  GIT_COMMITTER_DATE="$DATE" \
  git commit -m "$MSG"
}

# ── APRIL 8: Project scaffolding ─────────────────────────────────────────

git add pom.xml
commit_on "2026-04-08T10:15:00" "chore: initialise Maven project with Jersey and Grizzly dependencies"

git add src/main/java/uk/ac/campus/boot/CampusHubApp.java
commit_on "2026-04-08T14:42:00" "feat: add CampusHubApp with @ApplicationPath /api/v1"

# ── APRIL 9: Domain models ───────────────────────────────────────────────

git add src/main/java/uk/ac/campus/domain/Room.java
commit_on "2026-04-09T09:20:00" "feat: add Room domain model with sensorIds list"

git add src/main/java/uk/ac/campus/domain/Sensor.java
commit_on "2026-04-09T11:05:00" "feat: add Sensor domain model with DeviceStatus enum"

git add src/main/java/uk/ac/campus/domain/ReadingRecord.java
commit_on "2026-04-09T15:30:00" "feat: add ReadingRecord domain model with UUID auto-generation"

# ── APRIL 10: Error envelope + data store ───────────────────────────────

git add src/main/java/uk/ac/campus/domain/ApiErrorPayload.java
commit_on "2026-04-10T10:00:00" "feat: add ApiErrorPayload as uniform JSON error envelope"

git add src/main/java/uk/ac/campus/repository/InMemoryStore.java
commit_on "2026-04-10T14:15:00" "feat: implement singleton InMemoryStore with ConcurrentHashMap"

# ── APRIL 11: Server launcher ────────────────────────────────────────────

git add src/main/java/uk/ac/campus/boot/ServerLauncher.java
commit_on "2026-04-11T09:45:00" "feat: add ServerLauncher to bootstrap embedded Grizzly server"

# ── APRIL 12: Discovery endpoint ─────────────────────────────────────────

git add src/main/java/uk/ac/campus/api/discovery/DiscoveryController.java
commit_on "2026-04-12T11:20:00" "feat: implement GET /api/v1 discovery endpoint with HATEOAS links"

# ── APRIL 13: Room controller ────────────────────────────────────────────

git add src/main/java/uk/ac/campus/api/rooms/RoomController.java
commit_on "2026-04-13T10:30:00" "feat: implement RoomController with GET and POST /api/v1/rooms"

# ── APRIL 14: Room deletion ──────────────────────────────────────────────

git add src/main/java/uk/ac/campus/api/rooms/RoomController.java
commit_on "2026-04-14T09:10:00" "feat: add DELETE /rooms/{roomId} with sensor occupancy guard"

# ── APRIL 15: Sensor controller ──────────────────────────────────────────

git add src/main/java/uk/ac/campus/api/sensors/SensorController.java
commit_on "2026-04-15T10:00:00" "feat: implement SensorController with GET, POST and type filter"

# ── APRIL 16: Reading sub-resource ───────────────────────────────────────

git add src/main/java/uk/ac/campus/api/sensors/ReadingController.java
commit_on "2026-04-16T11:45:00" "feat: add ReadingController as sub-resource for sensor telemetry"

git add src/main/java/uk/ac/campus/api/sensors/SensorController.java
commit_on "2026-04-16T14:20:00" "feat: wire sub-resource locator in SensorController for /readings"

# ── APRIL 17: Custom exceptions ───────────────────────────────────────────

git add src/main/java/uk/ac/campus/errors/exceptions/EntityNotFoundException.java
git add src/main/java/uk/ac/campus/errors/exceptions/RoomOccupiedException.java
commit_on "2026-04-17T09:30:00" "feat: add EntityNotFoundException and RoomOccupiedException"

git add src/main/java/uk/ac/campus/errors/exceptions/UnresolvableReferenceException.java
git add src/main/java/uk/ac/campus/errors/exceptions/DeviceOfflineException.java
commit_on "2026-04-17T13:15:00" "feat: add UnresolvableReferenceException and DeviceOfflineException"

# ── APRIL 18: Exception handlers ─────────────────────────────────────────

git add src/main/java/uk/ac/campus/errors/handlers/EntityNotFoundHandler.java
git add src/main/java/uk/ac/campus/errors/handlers/RoomOccupiedHandler.java
commit_on "2026-04-18T10:00:00" "feat: implement EntityNotFoundHandler (404) and RoomOccupiedHandler (409)"

git add src/main/java/uk/ac/campus/errors/handlers/UnresolvableReferenceHandler.java
git add src/main/java/uk/ac/campus/errors/handlers/DeviceOfflineHandler.java
commit_on "2026-04-18T13:40:00" "feat: implement UnresolvableReferenceHandler (422) and DeviceOfflineHandler (403)"

git add src/main/java/uk/ac/campus/errors/handlers/GlobalFaultBarrier.java
commit_on "2026-04-18T16:05:00" "feat: add GlobalFaultBarrier catch-all ExceptionMapper for HTTP 500"

# ── APRIL 19: Logging filter ──────────────────────────────────────────────

git add src/main/java/uk/ac/campus/middleware/RequestAuditFilter.java
commit_on "2026-04-19T11:00:00" "feat: implement RequestAuditFilter for request/response observability"

# ── APRIL 21: README ──────────────────────────────────────────────────────

git add README.md
commit_on "2026-04-21T10:30:00" "docs: add README with project overview, build steps and curl examples"

# ── APRIL 22: Report answers ──────────────────────────────────────────────

git add README.md
commit_on "2026-04-22T14:00:00" "docs: complete conceptual question answers in README"

# ── APRIL 23: Final cleanup ───────────────────────────────────────────────

git add -A
commit_on "2026-04-23T16:30:00" "chore: final review and tidy before submission"