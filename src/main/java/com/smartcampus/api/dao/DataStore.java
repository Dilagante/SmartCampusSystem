package com.smartcampus.api.dao;

import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private to prevent external instantiation
    private DataStore() {
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    /* --- ROOM OPERATIONS --- */

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room findRoomById(String id) {
        return rooms.get(id);
    }

    public void saveRoom(Room r) {
        rooms.put(r.getId(), r);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    /* --- SENSOR OPERATIONS --- */

    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    public Sensor findSensorById(String id) {
        return sensors.get(id);
    }

    public void saveSensor(Sensor s) {
        sensors.put(s.getId(), s);
    }

    public boolean deleteSensor(String id) {
        return sensors.remove(id) != null;
    }

    /* --- READING OPERATIONS --- */

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        // computeIfAbsent is thread-safe with ConcurrentHashMap
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}