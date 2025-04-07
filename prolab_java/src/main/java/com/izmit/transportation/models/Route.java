package com.izmit.transportation.models;

import java.util.List;

public class Route {
    private List<RouteSegment> segments;
    private TransportationType transportType;
    private double totalDistance;
    private double totalTime;
    
    public Route(List<RouteSegment> segments, TransportationType transportType) {
        this.segments = segments;
        this.transportType = transportType;
        calculateTotals();
    }
    
    private void calculateTotals() {
        totalDistance = segments.stream()
            .mapToDouble(RouteSegment::getDistance)
            .sum();
            
        totalTime = segments.stream()
            .mapToDouble(RouteSegment::getTime)
            .sum();
    }
    
    public List<RouteSegment> getSegments() {
        return segments;
    }
    
    public TransportationType getTransportType() {
        return transportType;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public double getTotalTime() {
        return totalTime;
    }
} 