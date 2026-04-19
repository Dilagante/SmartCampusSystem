package com.smartcampus.api.dao;

import com.smartcampus.api.model.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorDAO {

    private final Map<String, Sensor> store = new ConcurrentHashMap<>();

    public List<Sensor> getAll() {
        return new ArrayList<>(store.values());
    }

    public Sensor findById(String id) {
        return store.get(id);
    }

    public void add(Sensor Sensor) {
        store.put(Sensor.getId(), Sensor);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

}
