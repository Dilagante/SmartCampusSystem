package com.smartcampus.api.database;

import com.smartcampus.api.model.*; //We use all since we know what's in the package
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap; //To handle race conditions and simultaneous API calls

public class DataStore {

    private static Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // We use Private to prevent anyone from instantiating this class
    private DataStore() {

    }

    // Generic Getter for Rooms
    public static Map<String, Room> getRooms() {
        return rooms;
    }

    // Generic Getter for Sensors
    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    // Encapsulated method to add Rooms
    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    // Get a specific room by its ID
    public static Room getRoomById(String id) {
        return rooms.get(id);
    }

    // Encapsulated method to add Sensors
    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(),sensor);
    }

    // Get a specific sensor by its ID
    public static Sensor getSensorById(String id) {
        return sensors.get(id);
    }


}
