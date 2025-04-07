package com.izmit.transportation.models;

import java.util.List;

public class RouteOption {
    private final List<RouteSegment> segments;

    public RouteOption(List<RouteSegment> segments) {
        this.segments = segments;
    }

    public List<RouteSegment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rota Seçeneği:\n");
        
        double totalDistance = 0;
        double totalCost = 0;
        double totalTime = 0;
        
        for (RouteSegment segment : segments) {
            sb.append(segment.toString()).append("\n");
            totalDistance += segment.getDistance();
            totalCost += segment.getCost();
            totalTime += segment.getTime();
        }
        
        sb.append(String.format("\nToplam: %.2f km, %.1f dk, %.2f TL",
            totalDistance, totalTime, totalCost));
        
        return sb.toString();
    }
} 