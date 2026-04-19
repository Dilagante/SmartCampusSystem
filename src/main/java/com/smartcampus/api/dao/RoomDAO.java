package com.smartcampus.api.dao;

import com.smartcampus.api.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomDAO {

    private final Map<String, Room> store = new ConcurrentHashMap<>();

    public List<Room> getAll() {
        return new ArrayList<>(store.values());
    }

    public Room findById(String id) {
        return store.get(id);
    }

    public void add(Room room) {
        store.put(room.getId(), room);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

}
