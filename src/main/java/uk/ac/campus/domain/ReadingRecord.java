package uk.ac.campus.domain;

import java.util.UUID;

/**
 * An immutable snapshot of a sensor measurement at a specific point in time.
 * Each record is assigned a unique identifier on creation.
 */
public class ReadingRecord {

    private String id;
    private long timestamp;
    private double value;

    public ReadingRecord() {}

    /**
     * Creates a new reading with an auto-generated UUID and current epoch time.
     */
    public ReadingRecord(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public ReadingRecord(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // --- Getters / Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
