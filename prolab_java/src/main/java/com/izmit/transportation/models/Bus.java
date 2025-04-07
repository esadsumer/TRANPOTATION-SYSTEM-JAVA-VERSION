package com.izmit.transportation.models;

import com.izmit.transportation.abstracts.AbstractTransportation;
import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.Location;
import java.util.List;

public class Bus extends AbstractTransportation {
    private List<Stop> stops;
    private double baseFare;
    
    public Bus(String id, Location location) {
        this.id = id;
        this.type = TransportationType.BUS;
        this.currentLocation = location;
        this.status = VehicleStatus.AVAILABLE;
        this.baseFare = 7.5; // TL
    }
    
    @Override
    public double calculateFare(Route route) {
        return baseFare * route.getTotalDistance();
    }
    
    @Override
    public Route calculateRoute(Location start, Location end) {
        // Otobüs rota hesaplama mantığı
        return null; // TODO: Implement
    }
    
    @Override
    public boolean validateForRoute(Route route) {
        return route.getTransportType() == TransportationType.BUS;
    }
    
    public List<Stop> getStops() {
        return stops;
    }
    
    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }
} 