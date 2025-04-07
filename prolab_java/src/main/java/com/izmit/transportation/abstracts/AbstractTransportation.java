package com.izmit.transportation.abstracts;

import com.izmit.transportation.interfaces.TransportationInterface;
import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.Location;
import com.izmit.transportation.models.TransportationType;
import com.izmit.transportation.models.VehicleStatus;

public abstract class AbstractTransportation implements TransportationInterface {
    protected String id;
    protected TransportationType type;
    protected Location currentLocation;
    protected VehicleStatus status;
    
    @Override
    public String getVehicleInfo() {
        return "ID: " + id + ", Type: " + type;
    }
    
    public TransportationType getType() {
        return type;
    }
    
    public Location getCurrentLocation() {
        return currentLocation;
    }
    
    public VehicleStatus getStatus() {
        return status;
    }
} 