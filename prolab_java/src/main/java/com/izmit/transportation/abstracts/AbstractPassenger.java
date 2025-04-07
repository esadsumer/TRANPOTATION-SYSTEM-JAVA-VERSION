package com.izmit.transportation.abstracts;

import com.izmit.transportation.models.PassengerType;
import com.izmit.transportation.models.TransportationType;

public abstract class AbstractPassenger {
    protected String id;
    protected PassengerType type;
    
    public abstract double getDiscountRate();
    public abstract boolean isValidForTransport(TransportationType type);
    
    public PassengerType getType() {
        return type;
    }
    
    public String getId() {
        return id;
    }
} 