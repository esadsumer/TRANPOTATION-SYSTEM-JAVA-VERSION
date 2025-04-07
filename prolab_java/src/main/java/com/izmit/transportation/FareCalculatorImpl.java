package com.izmit.transportation;

import com.izmit.transportation.interfaces.FareCalculator;
import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.PassengerType;
import com.izmit.transportation.models.TransportationType;

public class FareCalculatorImpl implements FareCalculator {
    @Override
    public double calculateFare(Route route) {
        return route.getTotalDistance() * getBaseFare(route.getTransportType());
    }
    
    @Override
    public double applyDiscount(double fare, PassengerType type) {
        return fare * (1 - getDiscountRate(type));
    }
    
    private double getBaseFare(TransportationType type) {
        switch(type) {
            case BUS: return 7.5;
            case TRAM: return 5.0;
            case TAXI: return 10.0;
            case AUTONOMOUS_TAXI: return 12.0;
            case AUTONOMOUS_BUS: return 8.0;
            default: return 0.0;
        }
    }
    
    private double getDiscountRate(PassengerType type) {
        switch(type) {
            case STUDENT: return 0.5; // %50
            case ELDERLY: return 0.7; // %70
            case DISABLED: return 0.7; // %70
            default: return 0.0; // %0
        }
    }
} 