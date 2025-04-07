package com.izmit.transportation.interfaces;

import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.Location;

public interface RouteCalculator {
    Route findRoute(Location start, Location end);
    boolean validateRoute(Route route);
} 