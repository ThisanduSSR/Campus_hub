package uk.ac.campus.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a physical room in the campus building.
 * Tracks which sensor devices are deployed within it.
 */
public class Room {

    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds;

    public Room() {
        this.sensorIds = new ArrayList<>();
    }

    public Room(String id, String name, int capacity) {
        this();
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public boolean hasDevices() {
        return sensorIds != null && !sensorIds.isEmpty();
    }

    public void attachDevice(String deviceId) {
        if (sensorIds == null) {
            sensorIds = new ArrayList<>();
        }
        sensorIds.add(deviceId);
    }

    public void detachDevice(String deviceId) {
        if (sensorIds != null) {
            sensorIds.remove(deviceId);
        }
    }

    public List<String> getDeviceSnapshot() {
        return sensorIds == null ? Collections.emptyList() : Collections.unmodifiableList(sensorIds);
    }

    // --- Standard getters/setters required for JAX-RS JSON serialization ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
