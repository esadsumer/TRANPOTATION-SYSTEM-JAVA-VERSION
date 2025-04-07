package com.izmit.transportation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izmit.transportation.models.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.stream.Collectors;
import java.io.FileReader;
import java.io.PrintStream;
import com.izmit.transportation.interfaces.TransportationInterface;
import com.izmit.transportation.abstracts.AbstractPassenger;
import com.izmit.transportation.models.Route;
import com.izmit.transportation.models.Location;
import com.izmit.transportation.models.TransportationType;
import com.izmit.transportation.interfaces.FareCalculator;

public class TransportationSystem {
    private static final double WALKING_SPEED = 5.0; // km/saat
    private static final double TAXI_THRESHOLD = 1.5; // km - Durağa yürüme mesafesi eşiği
    private static final double TRANSFER_TIME = 5.0; // dakika - Aktarma süresi
    private static final double TRANSFER_DISCOUNT = 0.5; // %50 indirim
    private static final double TRANSFER_BONUS = 2.0; // 2 TL bonus
    private static final double TAXI_STARTING_FARE = 10.0; // Taksi açılış ücreti
    private static final double TAXI_PER_KM_FARE = 2.0; // Kilometre başına taksi ücreti
    private static final double MAX_WALKING_DISTANCE = 3.0; // Maksimum yürüme mesafesi (km)
    private static final double DEFAULT_FARE = 1.0; // Varsayılan ücret

    private final List<Stop> stops;
    private final Map<String, Stop> stopMap;
    private final Map<String, Double> taxiFares;
    private double taxiBasePrice;
    private double taxiPricePerKm;
    private final String city;
    private List<TransportationInterface> vehicles;
    private List<AbstractPassenger> passengers;
    private FareCalculator fareCalculator;

    public TransportationSystem() {
        this.stops = new ArrayList<>();
        this.stopMap = new HashMap<>();
        this.taxiFares = new HashMap<>();
        this.taxiBasePrice = 10.0;
        this.taxiPricePerKm = 5.0;
        this.city = "İzmit";
        this.vehicles = new ArrayList<>();
        this.passengers = new ArrayList<>();
        this.fareCalculator = new FareCalculatorImpl();
        loadData();
    }

    public void loadData() {
        try {
            // Konsol çıktısı için UTF-8 kodlamasını ayarla
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            
            // JSON dosyasını UTF-8 ile oku
            String jsonContent = new String(getClass().getResourceAsStream("/Duraklar.json").readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            JsonObject jsonData = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            // Durakları yükle
            JsonArray duraklar = jsonData.getAsJsonArray("duraklar");
            for (JsonElement element : duraklar) {
                JsonObject durak = element.getAsJsonObject();
                String id = durak.get("id").getAsString();
                String name = durak.get("name").getAsString();
                String type = durak.get("type").getAsString();
                boolean isSonDurak = durak.get("sonDurak").getAsBoolean();
                JsonObject locationObj = durak.getAsJsonObject("location");
                double lat = locationObj.get("lat").getAsDouble();
                double lon = locationObj.get("lon").getAsDouble();
                Location location = new Location(lat, lon);
                
                // Sonraki durakları yükle
                List<Map<String, Object>> nextStops = new ArrayList<>();
                JsonArray nextStopsArray = durak.getAsJsonArray("nextStops");
                if (nextStopsArray != null) {
                    for (JsonElement nextStopElement : nextStopsArray) {
                        JsonObject nextStopObj = nextStopElement.getAsJsonObject();
                        Map<String, Object> nextStopMap = new HashMap<>();
                        nextStopMap.put("stopId", nextStopObj.get("stopId").getAsString());
                        nextStopMap.put("mesafe", nextStopObj.get("mesafe").getAsDouble());
                        nextStopMap.put("ucret", nextStopObj.get("ucret").getAsDouble());
                        nextStopMap.put("sure", nextStopObj.get("sure").getAsDouble());
                        nextStops.add(nextStopMap);
                    }
                }
                
                // Transfer bilgilerini yükle
                Map<String, Object> transfer = new HashMap<>();
                JsonObject transferObj = durak.getAsJsonObject("transfer");
                if (transferObj != null) {
                    transfer.put("transferStopId", transferObj.get("transferStopId").getAsString());
                    transfer.put("transferSure", transferObj.get("transferSure").getAsDouble());
                    transfer.put("transferUcret", transferObj.get("transferUcret").getAsDouble());
                }
                
                // Durak tipine göre ismi güncelle
                String displayName = name;
                if (type.equals("bus")) {
                    displayName = "🚌 " + name + " (Otobüs)";
                } else if (type.equals("tram")) {
                    displayName = "🚊 " + name + " (Tramvay)";
                }
                
                Stop stop = new Stop(id, displayName, type, location, isSonDurak, nextStops, transfer);
                stops.add(stop);
                stopMap.put(id, stop);
            }
            
            // Taksi ayarlarını yükle
            JsonObject taxi = jsonData.getAsJsonObject("taxi");
            if (taxi != null) {
                this.taxiBasePrice = taxi.get("openingFee").getAsDouble();
                this.taxiPricePerKm = taxi.get("costPerKm").getAsDouble();
            }
            
            System.out.println("Toplam " + stops.size() + " durak yüklendi.");
            
        } catch (Exception e) {
            System.err.println("Veri yükleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Stop> getStops() {
        return stops;
    }

    public Stop getStop(String id) {
        return stopMap.get(id);
    }

    public List<RouteOption> findRoutes(Stop startStop, Stop endStop) {
        List<RouteOption> routes = new ArrayList<>();
        List<RouteSegment> currentRoute = new ArrayList<>();
        Set<Stop> visited = new HashSet<>();

        // Başlangıç durağından tüm rota seçeneklerini bul
        findRoutesRecursive(startStop, endStop, currentRoute, visited, routes);

        return routes;
    }

    private void findRoutesRecursive(Stop currentStop, Stop endStop,
                                   List<RouteSegment> currentRoute, Set<Stop> visited,
                                   List<RouteOption> routes) {
        if (currentStop.equals(endStop)) {
            routes.add(new RouteOption(new ArrayList<>(currentRoute)));
            return;
        }

        visited.add(currentStop);

        // Mevcut duraktan gidilebilecek tüm durakları kontrol et
        for (Map<String, Object> nextStopInfo : currentStop.getNextStops()) {
            String nextStopId = (String) nextStopInfo.get("stopId");
            Stop nextStop = getStop(nextStopId);
            
            if (nextStop != null && !visited.contains(nextStop)) {
                // Aynı tipteki araçlarla direkt bağlantı
                if (currentStop.getType().equals(nextStop.getType())) {
                    RouteSegment segment = createRouteSegment(currentStop, nextStop, false);
                    currentRoute.add(segment);
                    findRoutesRecursive(nextStop, endStop, currentRoute, visited, routes);
                    currentRoute.remove(currentRoute.size() - 1);
                }

                // Transfer noktaları üzerinden bağlantı
                if (currentStop.getTransfer() != null) {
                    String transferStopId = (String) currentStop.getTransfer().get("transferStopId");
            Stop transferStop = getStop(transferStopId);
                    
            if (transferStop != null && !visited.contains(transferStop)) {
                        RouteSegment currentSegment = createRouteSegment(currentStop, transferStop, true);
                        currentRoute.add(currentSegment);
                        findRoutesRecursive(transferStop, endStop, currentRoute, visited, routes);
                    currentRoute.remove(currentRoute.size() - 1);
                }
                }
            }
        }
    }

    public double calculateTotalCost(RouteOption route) {
        double totalCost = 0.0;
        RouteSegment previousSegment = null;

        for (RouteSegment segment : route.getSegments()) {
            totalCost += segment.getCost();

            if (previousSegment != null && segment.isTransfer()) {
                totalCost += calculateTransferCost(previousSegment, segment);
            }

            previousSegment = segment;
        }

        return totalCost;
    }

    public double calculateTotalTime(RouteOption route) {
        double totalTime = 0.0;
        RouteSegment previousSegment = null;

        for (RouteSegment segment : route.getSegments()) {
            totalTime += segment.getTime();

            if (previousSegment != null && segment.isTransfer()) {
                totalTime += TRANSFER_TIME;
            }

            previousSegment = segment;
        }

        return totalTime;
    }

    public double calculateTotalDistance(RouteOption route) {
        return route.getSegments().stream()
                .mapToDouble(RouteSegment::getDistance)
                .sum();
    }

    public double calculateTaxiFare(Location from, Location to) {
        double distance = calculateDistance(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
        return taxiBasePrice + (distance * taxiPricePerKm);
    }

    private Stop findNearestStop(Location location) {
        Stop nearestStop = null;
        double minDistance = Double.MAX_VALUE;

        for (Stop stop : stops) {
            double distance = calculateDistance(
                location.getLatitude(), location.getLongitude(),
                stop.getLocation().getLatitude(), stop.getLocation().getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearestStop = stop;
            }
        }

        return nearestStop;
    }

    public List<RouteSegment> getNextStops(Stop currentStop) {
        List<RouteSegment> segments = new ArrayList<>();

        for (Map<String, Object> nextStopInfo : currentStop.getNextStops()) {
            String nextStopId = (String) nextStopInfo.get("stopId");
            Stop nextStop = getStop(nextStopId);
            
            if (nextStop != null) {
                double distance = (Double) nextStopInfo.get("mesafe");
                double cost = (Double) nextStopInfo.get("ucret");
                double time = (Double) nextStopInfo.get("sure");
                
                segments.add(new RouteSegment(currentStop, nextStop, distance, cost, time, false));
            }
        }

        return segments;
    }

    public List<Stop> getAllStops() {
        return new ArrayList<>(stops);
    }

    public Stop getStopById(String id) {
        return stopMap.get(id);
    }

    private RouteSegment createRouteSegment(Stop fromStop, Stop toStop, boolean isTransfer) {
        double distance = calculateDistance(
            fromStop.getLocation().getLatitude(), fromStop.getLocation().getLongitude(),
            toStop.getLocation().getLatitude(), toStop.getLocation().getLongitude()
        );
        
        double cost = DEFAULT_FARE;
        double time = distance / WALKING_SPEED * 60; // dakika cinsinden
        
        if (isTransfer) {
            Map<String, Object> transfer = fromStop.getTransfer();
            if (transfer != null) {
                cost = (Double) transfer.get("transferUcret");
                time = (Double) transfer.get("transferSure");
            }
        } else {
            for (Map<String, Object> nextStop : fromStop.getNextStops()) {
                if (nextStop.get("stopId").equals(toStop.getId())) {
                    cost = (Double) nextStop.get("ucret");
                    time = (Double) nextStop.get("sure");
                    break;
                }
            }
        }
        
        return new RouteSegment(fromStop, toStop, distance, cost, time, isTransfer);
    }

    private List<Stop> findShortestPath(Stop start, Stop end) {
        Map<Stop, Double> distances = new HashMap<>();
        Map<Stop, Stop> previousStops = new HashMap<>();
        PriorityQueue<Stop> queue = new PriorityQueue<>(
            (a, b) -> Double.compare(distances.getOrDefault(a, Double.MAX_VALUE),
                                  distances.getOrDefault(b, Double.MAX_VALUE))
        );
        Set<Stop> visited = new HashSet<>();

        // Başlangıç mesafelerini ayarla
        for (Stop stop : stops) {
            distances.put(stop, Double.MAX_VALUE);
            previousStops.put(stop, null);
        }
        distances.put(start, 0.0);

        queue.offer(start);

        while (!queue.isEmpty()) {
            Stop current = queue.poll();
            if (current.equals(end)) {
                System.out.println("Rota bulundu: " + start.getName() + " -> " + end.getName());
                break;
            }

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            // Direkt bağlantıları kontrol et
            for (Map<String, Object> nextStop : current.getNextStops()) {
                String nextStopId = (String) nextStop.get("stopId");
                Stop neighbor = getStop(nextStopId);
                Double distance = (Double) nextStop.get("mesafe");
                
                if (neighbor != null && distance != null && !visited.contains(neighbor)) {
                    double newDistance = distances.get(current) + distance;
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        previousStops.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }

            // Transfer noktalarını kontrol et
            Map<String, Object> transfer = current.getTransfer();
            if (transfer != null && transfer.containsKey("transferStopId")) {
                String transferStopId = (String) transfer.get("transferStopId");
                Stop transferStop = getStop(transferStopId);
                Double transferDistance = (Double) transfer.get("mesafe");
                
                if (transferStop != null && !visited.contains(transferStop)) {
                    double newDistance = distances.get(current) + (transferDistance != null ? transferDistance : 0.0);
                    if (newDistance < distances.get(transferStop)) {
                        distances.put(transferStop, newDistance);
                        previousStops.put(transferStop, current);
                        queue.offer(transferStop);
                    }
                }
            }
        }

        // Rota bulunamadıysa
        if (distances.get(end) == Double.MAX_VALUE) {
            System.out.println("Rota bulunamadı: " + start.getName() + " -> " + end.getName());
            return null;
        }

        // Rotayı oluştur
        List<Stop> path = new ArrayList<>();
        Stop current = end;
        while (current != null) {
            path.add(0, current);
            current = previousStops.get(current);
        }

        return path;
    }

    private Map<String, Object> findConnection(Stop from, Stop to) {
        // Direkt bağlantıları kontrol et
        for (Map<String, Object> connection : from.getNextStops()) {
            if (connection.get("stopId").equals(to.getId())) {
                return connection;
            }
        }
        
        // Transfer bağlantısını kontrol et
        if (from.getTransfer() != null && from.getTransfer().get("transferStopId").equals(to.getId())) {
            return from.getTransfer();
        }
        
        return null;
    }

    public RouteOption findRoute(Location startLocation, Location endLocation, PaymentMethod paymentMethod) {
        // En yakın durakları bul
        Stop startStop = findNearestStop(startLocation);
        Stop endStop = findNearestStop(endLocation);

        if (startStop == null || endStop == null) {
            System.out.println("En yakın durak bulunamadı");
            return createTaxiRoute(startLocation, endLocation);
        }

        System.out.println("En yakın duraklar bulundu:");
        System.out.println("Başlangıç: " + startStop.getName());
        System.out.println("Varış: " + endStop.getName());

        // Başlangıç ve bitiş noktalarına yürüme mesafelerini hesapla
        double startWalkingDistance = calculateDistance(
            startLocation.getLatitude(), startLocation.getLongitude(),
            startStop.getLocation().getLatitude(), startStop.getLocation().getLongitude()
        );
        double endWalkingDistance = calculateDistance(
            endLocation.getLatitude(), endLocation.getLongitude(),
            endStop.getLocation().getLatitude(), endStop.getLocation().getLongitude()
        );

        System.out.println("Yürüme mesafeleri:");
        System.out.println("Başlangıç: " + startWalkingDistance + " km");
        System.out.println("Varış: " + endWalkingDistance + " km");

        // Eğer yürüme mesafesi çok uzunsa taksi kullan
        if (startWalkingDistance > MAX_WALKING_DISTANCE || endWalkingDistance > MAX_WALKING_DISTANCE) {
            System.out.println("Yürüme mesafesi çok uzun, taksi kullanılacak");
            return createTaxiRoute(startLocation, endLocation);
        }

        // En kısa rotayı bul
        List<Stop> path = findShortestPath(startStop, endStop);
        if (path == null || path.isEmpty()) {
            System.out.println("Toplu taşıma rotası bulunamadı, taksi kullanılacak");
            return createTaxiRoute(startLocation, endLocation);
        }

        // Rota segmentlerini oluştur
        List<RouteSegment> segments = new ArrayList<>();

        // Başlangıç noktasından ilk durağa yürüme
        if (startWalkingDistance > 0) {
            // Yürüme başlangıç durağını oluştur
            Stop walkingStart = new Stop(
                "walking_start",
                "🚶 Yürüme Başlangıç",
                "walking",
                startLocation,
                false,
                new ArrayList<>(),
                null
            );
            
            segments.add(new RouteSegment(
                walkingStart,
                startStop,
                startWalkingDistance,
                startWalkingDistance * DEFAULT_FARE,
                (startWalkingDistance / WALKING_SPEED) * 60,
                false
            ));
        }

        // Duraklar arası seyahat
        for (int i = 0; i < path.size() - 1; i++) {
            Stop current = path.get(i);
            Stop next = path.get(i + 1);
            
            // Bağlantı bilgilerini bul
            Map<String, Object> connection = findConnection(current, next);
            if (connection != null) {
                double distance = 0.0;
                double cost = DEFAULT_FARE;
                double time = 0.0;
                
                // Bağlantı bilgilerini kontrol et
                if (connection.containsKey("mesafe")) {
                    distance = ((Number) connection.get("mesafe")).doubleValue();
                } else {
                    distance = calculateDistance(
                        current.getLocation().getLatitude(), current.getLocation().getLongitude(),
                        next.getLocation().getLatitude(), next.getLocation().getLongitude()
                    );
                }
                
                if (connection.containsKey("ucret")) {
                    cost = ((Number) connection.get("ucret")).doubleValue();
                } else {
                    cost = distance * DEFAULT_FARE;
                }
                
                if (connection.containsKey("sure")) {
                    time = ((Number) connection.get("sure")).doubleValue();
                } else {
                    // Ortalama hız 30 km/saat
                    time = (distance / 30.0) * 60;
                }
                
                // Transfer kontrolü
                boolean isTransfer = false;
                Map<String, Object> transfer = current.getTransfer();
                if (transfer != null && transfer.containsKey("transferStopId") && 
                    transfer.get("transferStopId").equals(next.getId())) {
                    isTransfer = true;
                    // Transfer ücreti ve süresini kullan
                    if (transfer.containsKey("transferUcret")) {
                        cost = ((Number) transfer.get("transferUcret")).doubleValue();
                    }
                    if (transfer.containsKey("transferSure")) {
                        time = ((Number) transfer.get("transferSure")).doubleValue();
                    }
                }
                
                // Ödeme yöntemine göre indirim uygula
                if (paymentMethod == PaymentMethod.KENTKART) {
                    cost *= 0.75; // %25 indirim
                }
                
                segments.add(new RouteSegment(current, next, distance, cost, time, isTransfer));
            } else {
                // Bağlantı bilgisi yoksa mesafeyi hesapla
                double distance = calculateDistance(
                    current.getLocation().getLatitude(), current.getLocation().getLongitude(),
                    next.getLocation().getLatitude(), next.getLocation().getLongitude()
                );
                double cost = distance * DEFAULT_FARE;
                double time = (distance / 30.0) * 60; // Ortalama hız 30 km/saat
                
                segments.add(new RouteSegment(current, next, distance, cost, time, false));
            }
        }

        // Son duraktan varış noktasına yürüme
        if (endWalkingDistance > 0) {
            // Yürüme varış durağını oluştur
            Stop walkingEnd = new Stop(
                "walking_end",
                "🚶 Yürüme Varış",
                "walking",
                endLocation,
                false,
                new ArrayList<>(),
                null
            );
            
            segments.add(new RouteSegment(
                endStop,
                walkingEnd,
                endWalkingDistance,
                endWalkingDistance * DEFAULT_FARE,
                (endWalkingDistance / WALKING_SPEED) * 60,
                false
            ));
        }

        System.out.println("Rota segmentleri oluşturuldu: " + segments.size() + " segment");
        return new RouteOption(segments);
    }

    private RouteOption createTaxiRoute(Location startLocation, Location endLocation) {
        double distance = calculateDistance(
            startLocation.getLatitude(), startLocation.getLongitude(),
            endLocation.getLatitude(), endLocation.getLongitude()
        );
        
        double cost = taxiBasePrice + (distance * taxiPricePerKm);
        double time = (distance / 50.0) * 60; // Taksi hızı ortalama 50 km/saat
        
        // Taksi başlangıç ve varış duraklarını oluştur
        Stop taxiStart = new Stop(
            "taxi_start",
            "🚕 Taksi Başlangıç",
            "taxi",
            startLocation,
            false,
            new ArrayList<>(),
            null
        );
        
        Stop taxiEnd = new Stop(
            "taxi_end",
            "🚕 Taksi Varış",
            "taxi",
            endLocation,
            false,
            new ArrayList<>(),
            null
        );
        
        List<RouteSegment> segments = new ArrayList<>();
        segments.add(new RouteSegment(
            taxiStart,
            taxiEnd,
            distance,
            cost,
            time,
            false
        ));
        
        return new RouteOption(segments);
    }

    public double calculateTransferCost(RouteSegment previousSegment, RouteSegment currentSegment) {
        double transferCost = TRANSFER_BONUS;
        
        if (previousSegment.isTransfer() || currentSegment.isTransfer()) {
            transferCost *= TRANSFER_DISCOUNT;
        }
        
        return transferCost;
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Dünya'nın yarıçapı (km)
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public double calculateTaxiFare(double distance) {
        return taxiBasePrice + (distance * taxiPricePerKm);
    }

    public boolean isWalkingDistance(double distance) {
        return distance <= MAX_WALKING_DISTANCE;
    }

    public List<RouteSegment> findRoute(double startLat, double startLon, double endLat, double endLon) {
        Location startLocation = new Location(startLat, startLon);
        Location endLocation = new Location(endLat, endLon);
        
        RouteOption route = findRoute(startLocation, endLocation, PaymentMethod.KENTKART);
        return route != null ? route.getSegments() : new ArrayList<>();
    }

    private void loadStops() {
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonData = parser.parse(new FileReader("src/main/resources/stops.json")).getAsJsonObject();
            JsonArray stopsArray = jsonData.getAsJsonArray("stops");

            for (JsonElement element : stopsArray) {
                JsonObject stopObj = element.getAsJsonObject();
                String id = stopObj.get("id").getAsString();
                String name = stopObj.get("name").getAsString();
                String type = stopObj.get("type").getAsString();
                JsonObject locationObj = stopObj.getAsJsonObject("location");
                double lat = locationObj.get("lat").getAsDouble();
                double lon = locationObj.get("lon").getAsDouble();
                Location location = new Location(lat, lon);

                Stop stop = new Stop(id, name, type, location);
                stops.add(stop);
            }

            // Duraklar arası bağlantıları oluştur
            for (int i = 0; i < stops.size(); i++) {
                for (int j = i + 1; j < stops.size(); j++) {
                    Stop stop1 = stops.get(i);
                    Stop stop2 = stops.get(j);
                    
                    // Aynı tipteki duraklar arasında bağlantı oluştur
                    if (stop1.getType().equals(stop2.getType())) {
                        double distance = calculateDistance(
                            stop1.getLocation().getLatitude(),
                            stop1.getLocation().getLongitude(),
                            stop2.getLocation().getLatitude(),
                            stop2.getLocation().getLongitude()
                        );
                        
                        // Maksimum yürüme mesafesi içindeyse bağlantı ekle
                        if (distance <= MAX_WALKING_DISTANCE) {
                            stop1.addConnection(stop2, distance);
                            stop2.addConnection(stop1, distance);
                        }
                    }
                }
            }

            System.out.println("Toplam " + stops.size() + " durak yüklendi.");
        } catch (Exception e) {
            System.err.println("Duraklar yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    public Route findRoute(Location start, Location end, 
                         TransportationType preferredType) {
        return vehicles.stream()
            .filter(v -> v.getType() == preferredType)
            .findFirst()
            .map(v -> v.calculateRoute(start, end))
            .orElse(null);
    }
    
    public double calculateTotalFare(Route route, AbstractPassenger passenger) {
        if (route == null || passenger == null) {
            return 0.0;
        }
        
        double baseFare = fareCalculator.calculateFare(route);
        return fareCalculator.applyDiscount(baseFare, passenger.getType());
    }
    
    public void addVehicle(TransportationInterface vehicle) {
        vehicles.add(vehicle);
    }
    
    public void addPassenger(AbstractPassenger passenger) {
        passengers.add(passenger);
    }
    
    public List<TransportationInterface> getAvailableVehicles() {
        return vehicles;
    }
    
    public List<AbstractPassenger> getPassengers() {
        return passengers;
    }
} 