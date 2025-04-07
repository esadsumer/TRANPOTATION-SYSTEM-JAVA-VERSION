package com.izmit.transportation.interfaces;

import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.PassengerType;

public interface FareCalculator {
    double calculateFare(Route route);
    double applyDiscount(double fare, PassengerType type);
} 