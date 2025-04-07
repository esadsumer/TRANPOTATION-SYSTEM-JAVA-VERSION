package com.izmit.transportation.models;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Stop {
    private String id;
    private String name;
    private String type;
    private Location location;
    private boolean isSonDurak;
    private List<Map<String, Object>> nextStops;
    private Map<String, Object> transfer;
    private Map<Stop, Double> connections = new HashMap<>();

    public Stop(String id, String name, String type, Location location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.isSonDurak = false;
        this.nextStops = new ArrayList<>();
        this.transfer = null;
    }

    public Stop(String id, String name, String type, Location location,
                boolean isSonDurak, List<Map<String, Object>> nextStops,
                Map<String, Object> transfer) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.isSonDurak = isSonDurak;
        this.nextStops = nextStops;
        this.transfer = transfer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isSonDurak() {
        return isSonDurak;
    }

    public void setSonDurak(boolean sonDurak) {
        isSonDurak = sonDurak;
    }

    public List<Map<String, Object>> getNextStops() {
        return nextStops;
    }

    public void setNextStops(List<Map<String, Object>> nextStops) {
        this.nextStops = nextStops;
    }

    public Map<String, Object> getTransfer() {
        return transfer;
    }

    public void setTransfer(Map<String, Object> transfer) {
        this.transfer = transfer;
    }

    public double getLatitude() {
        return location.getLatitude();
    }

    public double getLongitude() {
        return location.getLongitude();
    }

    public double distanceTo(Stop other) {
        return location.distanceTo(other.getLocation());
    }

    public Map<Stop, Double> getConnections() {
        return connections;
    }

    public void addConnection(Stop stop, double distance) {
        connections.put(stop, distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return id.equals(stop.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
} 