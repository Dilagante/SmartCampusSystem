package com.smartcampus.api.dao;

import com.smartcampus.api.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorReadingDAO {

    private final Map<String, SensorReading> store = new ConcurrentHashMap<>();

    public List<SensorReading> getAll() {
        return new ArrayList<>(store.values());
    }

    public SensorReading findById(String id) {
        return store.get(id);
    }

    public void add(SensorReading SensorReading) {
        store.put(SensorReading.getId(), SensorReading);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

}
