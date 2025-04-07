package com.izmit.transportation.models;

public class RouteSegment {
    private Stop fromStop;
    private Stop toStop;
    private double distance;
    private double time;
    private double cost;
    private boolean isTransfer;
    
    public RouteSegment(Stop fromStop, Stop toStop, double distance, 
                       double time, double cost, boolean isTransfer) {
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.distance = distance;
        this.time = time;
        this.cost = cost;
        this.isTransfer = isTransfer;
    }
    
    public Stop getFromStop() {
        return fromStop;
    }
    
    public Stop getToStop() {
        return toStop;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public double getTime() {
        return time;
    }
    
    public double getCost() {
        return cost;
    }
    
    public boolean isTransfer() {
        return isTransfer;
    }
    
    @Override
    public String toString() {
        return String.format("%s -> %s (%.2f km, %.1f dk, %.2f TL%s)",
            fromStop.getName(),
            toStop.getName(),
            distance,
            time,
            cost,
            isTransfer ? ", Transfer" : ""
        );
    }
} 