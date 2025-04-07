package com.izmit.transportation.interfaces;

import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.Location;
import com.izmit.transportation.models.TransportationType;

public interface TransportationInterface {
    double calculateFare(Route route);
    Route calculateRoute(Location start, Location end);
    boolean validateForRoute(Route route);
    String getVehicleInfo();
    TransportationType getType();
} 