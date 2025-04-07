package com.izmit.transportation;

import com.izmit.transportation.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportationSystemTest {
    private TransportationSystem system;
    private Stop stop1, stop2, stop3;

    @BeforeEach
    public void setUp() {
        system = new TransportationSystem();

        // Test durakları oluştur
        Location loc1 = new Location(40.7654, 29.9408);
        Location loc2 = new Location(40.7700, 29.9500);
        Location loc3 = new Location(40.7800, 29.9600);

        stop1 = new Stop("1", "İzmit Gar", "bus", loc1, true, new ArrayList<>(), new HashMap<>());
        stop2 = new Stop("2", "Yenişehir", "bus", loc2, false, new ArrayList<>(), new HashMap<>());
        stop3 = new Stop("3", "Üniversite", "tram", loc3, false, new ArrayList<>(), new HashMap<>());
    }

    @Test
    public void testFindRoute() {
        // Test verilerini hazırla
        List<RouteSegment> route = system.findRoute(40.7654, 29.9408, 40.7800, 29.9600);
        
        // Sonuçları kontrol et
        Assertions.assertNotNull(route);
        Assertions.assertFalse(route.isEmpty());
        
        // İlk segment kontrolü
        RouteSegment firstSegment = route.get(0);
        Assertions.assertEquals(40.7654, firstSegment.getStartLocation().getLat(), 0.0001);
        Assertions.assertEquals(29.9408, firstSegment.getStartLocation().getLon(), 0.0001);
        
        // Son segment kontrolü
        RouteSegment lastSegment = route.get(route.size() - 1);
        Assertions.assertEquals(40.7800, lastSegment.getEndLocation().getLat(), 0.0001);
        Assertions.assertEquals(29.9600, lastSegment.getEndLocation().getLon(), 0.0001);
    }

    @Test
    public void testCalculateDistance() {
        double distance = system.calculateDistance(40.7654, 29.9408, 40.7800, 29.9600);
        Assertions.assertTrue(distance > 0);
    }

    @Test
    public void testFindNearestStop() {
        Stop nearestStop = system.findNearestStop(40.7654, 29.9408);
        Assertions.assertNotNull(nearestStop);
    }
} 