package uk.ac.campus.repository;

import uk.ac.campus.domain.ReadingRecord;
import uk.ac.campus.domain.Room;
import uk.ac.campus.domain.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, singleton in-memory repository backing the entire API.
 *
 * JAX-RS creates a new resource class instance per request. This singleton
 * pattern ensures all those instances share the same state without needing
 * a database. ConcurrentHashMap is used throughout to handle concurrent
 * requests safely without explicit synchronisation blocks.
 */
public final class InMemoryStore {

    // Eager singleton — initialised once when the class is loaded
    private static final InMemoryStore INSTANCE = new InMemoryStore();

    private final ConcurrentHashMap<String, Room> roomRegistry       = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> deviceRegistry   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ReadingRecord>> telemetryLog = new ConcurrentHashMap<>();

    private InMemoryStore() {
        loadSampleData();
    }

    public static InMemoryStore getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Room operations
    // -------------------------------------------------------------------------

    public ConcurrentHashMap<String, Room> rooms() {
        return roomRegistry;
    }

    public Optional<Room> findRoom(String roomId) {
        return Optional.ofNullable(roomRegistry.get(roomId));
    }

    public boolean roomExists(String roomId) {
        return roomRegistry.containsKey(roomId);
    }

    public void saveRoom(Room room) {
        roomRegistry.put(room.getId(), room);
    }

    public boolean removeRoom(String roomId) {
        return roomRegistry.remove(roomId) != null;
    }

    // -------------------------------------------------------------------------
    // Sensor operations
    // -------------------------------------------------------------------------

    public ConcurrentHashMap<String, Sensor> sensors() {
        return deviceRegistry;
    }

    public Optional<Sensor> findSensor(String sensorId) {
        return Optional.ofNullable(deviceRegistry.get(sensorId));
    }

    public boolean sensorExists(String sensorId) {
        return deviceRegistry.containsKey(sensorId);
    }

    public void saveSensor(Sensor sensor) {
        deviceRegistry.put(sensor.getId(), sensor);
    }

    public boolean removeSensor(String sensorId) {
        return deviceRegistry.remove(sensorId) != null;
    }

    // -------------------------------------------------------------------------
    // Reading / Telemetry operations
    // -------------------------------------------------------------------------

    public List<ReadingRecord> getReadingsFor(String sensorId) {
        telemetryLog.putIfAbsent(sensorId, Collections.synchronizedList(new ArrayList<>()));
        return telemetryLog.get(sensorId);
    }

    public void appendReading(String sensorId, ReadingRecord record) {
        getReadingsFor(sensorId).add(record);
    }

    // -------------------------------------------------------------------------
    // Sample data bootstrap
    // -------------------------------------------------------------------------

    private void loadSampleData() {
        Room r1 = new Room("ENG-201", "Engineering Design Studio", 40);
        Room r2 = new Room("SCI-115", "Physics Lab Alpha", 25);
        saveRoom(r1);
        saveRoom(r2);

        Sensor s1 = new Sensor("HUM-001", "Humidity",    "ACTIVE",      65.3,  "ENG-201");
        Sensor s2 = new Sensor("LUX-001", "LightLevel",  "ACTIVE",      520.0, "ENG-201");
        Sensor s3 = new Sensor("CO2-002", "CO2",         "MAINTENANCE", 0.0,   "SCI-115");
        saveSensor(s1);
        saveSensor(s2);
        saveSensor(s3);

        r1.attachDevice(s1.getId());
        r1.attachDevice(s2.getId());
        r2.attachDevice(s3.getId());

        // Initialise empty telemetry logs
        getReadingsFor(s1.getId());
        getReadingsFor(s2.getId());
        getReadingsFor(s3.getId());
    }
}
