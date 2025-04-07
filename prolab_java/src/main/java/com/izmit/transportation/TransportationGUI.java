package com.izmit.transportation;

import com.izmit.transportation.models.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import com.google.gson.Gson;
import javafx.concurrent.Worker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;

public class TransportationGUI extends Application {
    private TransportationSystem system;
    private WebEngine webEngine;
    private ObservableList<Stop> stopsList;

    @FXML
    private WebView mapView;
    
    @FXML
    private TextField startLatField;
    
    @FXML
    private TextField startLonField;
    
    @FXML
    private TextField endLatField;
    
    @FXML
    private TextField endLonField;
    
    @FXML
    private ComboBox<Stop> startStopComboBox;
    
    @FXML
    private ComboBox<Stop> endStopComboBox;
    
    @FXML
    private ComboBox<PassengerType> passengerTypeComboBox;
    
    @FXML
    private ComboBox<PaymentMethod> paymentMethodComboBox;
    
    @FXML
    private TextArea resultText;

    @Override
    public void start(Stage primaryStage) throws Exception {
        system = new TransportationSystem();
        system.loadData();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transportation.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("İzmit Toplu Taşıma Sistemi");
        primaryStage.show();

        initializeMap();
        initializeStopComboBoxes();
        initializePassengerTypeComboBox();
        initializePaymentMethodComboBox();
    }

    private void initializePassengerTypeComboBox() {
        passengerTypeComboBox.getItems().addAll(
            PassengerType.NORMAL,
            PassengerType.STUDENT,
            PassengerType.ELDERLY,
            PassengerType.DISABLED
        );
        passengerTypeComboBox.setValue(PassengerType.NORMAL);
    }

    private String getPassengerTypeDisplayName(PassengerType type) {
        switch(type) {
            case NORMAL: return "Tam Bilet";
            case STUDENT: return "Öğrenci";
            case ELDERLY: return "Yaşlı";
            case DISABLED: return "Engelli";
            default: return type.toString();
        }
    }

    private void initializePaymentMethodComboBox() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodComboBox.setValue(PaymentMethod.KREDIKARTI); // Varsayılan değer
    }

    private void initializeStopComboBoxes() {
        stopsList = FXCollections.observableArrayList(system.getStops());
        startStopComboBox.setItems(stopsList);
        endStopComboBox.setItems(stopsList);

        // Durak seçildiğinde koordinatları otomatik doldur
        startStopComboBox.setOnAction(e -> {
            Stop selectedStop = startStopComboBox.getValue();
            if (selectedStop != null) {
                startLatField.setText(String.valueOf(selectedStop.getLatitude()));
                startLonField.setText(String.valueOf(selectedStop.getLongitude()));
            }
        });

        endStopComboBox.setOnAction(e -> {
            Stop selectedStop = endStopComboBox.getValue();
            if (selectedStop != null) {
                endLatField.setText(String.valueOf(selectedStop.getLatitude()));
                endLonField.setText(String.valueOf(selectedStop.getLongitude()));
            }
        });
    }

    @FXML
    private void calculateRoute() {
        try {
            // Başlangıç ve varış noktalarını al
            Location startLocation = new Location(
                Double.parseDouble(startLatField.getText()),
                Double.parseDouble(startLonField.getText())
            );
            Location endLocation = new Location(
                Double.parseDouble(endLatField.getText()),
                Double.parseDouble(endLonField.getText())
            );

            // Seçilen yolcu tipi ve ödeme yöntemini al
            PassengerType passengerType = passengerTypeComboBox.getValue();
            PaymentMethod paymentMethod = paymentMethodComboBox.getValue();

            // Rota hesapla
            RouteOption route = system.findRoute(startLocation, endLocation, paymentMethod);
            
            if (route != null && route.getSegments() != null && !route.getSegments().isEmpty()) {
                // Rota detaylarını göster
                StringBuilder details = new StringBuilder();
                details.append("=== ROTA DETAYLARI ===\n\n");
                
                double totalDistance = 0;
                double totalTime = 0;
                double totalCost = 0;

                // Her segment için detayları ekle
                for (RouteSegment segment : route.getSegments()) {
                    String fromName = segment.getFromStop() != null ? segment.getFromStop().getName() : "Başlangıç";
                    String toName = segment.getToStop() != null ? segment.getToStop().getName() : "Varış";
                    
                    // Segment tipine göre emoji ekle
                    String segmentType = "";
                    if (segment.getFromStop() != null && segment.getToStop() != null) {
                        if (segment.isTransfer()) {
                            segmentType = "🔄 Transfer: ";
                        } else if (segment.getFromStop().getType().equals("taxi") || 
                                 segment.getToStop().getType().equals("taxi")) {
                            segmentType = "🚕 Taksi: ";
                        } else if (segment.getFromStop().getType().equals("bus")) {
                            segmentType = "🚌 Otobüs: ";
                        } else {
                            segmentType = "🚊 Tramvay: ";
                        }
                    } else {
                        segmentType = "🚶 Yürüyüş: ";
                    }

                    details.append(String.format("%s%s -> %s\n", segmentType, fromName, toName));
                    details.append(String.format("   Mesafe: %.2f km\n", segment.getDistance()));
                    details.append(String.format("   Süre: %.1f dakika\n", segment.getTime()));
                    
                    // Segment ücretini hesapla
                    double segmentCost = segment.getCost();
                    if (passengerType != null) {
                        switch (passengerType) {
                            case STUDENT:
                                segmentCost *= 0.5; // %50 indirim
                                break;
                            case ELDERLY:
                            case DISABLED:
                                segmentCost *= 0.3; // %70 indirim
                                break;
                            default:
                                break;
                        }
                    }
                    
                    // Kentkart indirimi
                    if (paymentMethod == PaymentMethod.KENTKART) {
                        segmentCost *= 0.9; // %10 indirim
                    }
                    
                    details.append(String.format("   Ücret: %.2f TL\n", segmentCost));
                    details.append("\n");

                    totalDistance += segment.getDistance();
                    totalTime += segment.getTime();
                    totalCost += segmentCost;
                }

                // Toplam bilgileri ekle
                details.append("=== TOPLAM ===\n");
                details.append(String.format("Toplam Mesafe: %.2f km\n", totalDistance));
                details.append(String.format("Toplam Süre: %.1f dakika\n", totalTime));
                details.append(String.format("Toplam Ücret: %.2f TL\n", totalCost));

                // Yolcu tipi ve ödeme yöntemi bilgilerini ekle
                details.append("\n=== BİLET BİLGİLERİ ===\n");
                details.append(String.format("Yolcu Tipi: %s\n", getPassengerTypeDisplayName(passengerType)));
                details.append(String.format("Ödeme Yöntemi: %s\n", paymentMethod.toString()));

                // Rota detaylarını göster
                resultText.setText(details.toString());

                // Haritada rotayı göster
                displayRoute(route.getSegments());
            } else {
                resultText.setText("Rota bulunamadı! Lütfen farklı duraklar seçin veya koordinatları kontrol edin.");
            }
        } catch (NumberFormatException e) {
            resultText.setText("Lütfen geçerli koordinat değerleri girin!");
        } catch (Exception e) {
            resultText.setText("Bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeMap() {
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>İzmit Toplu Taşıma Haritası</title>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                <style>
                    #map { height: 100vh; width: 100%; }
                    .stop-marker {
                        width: 20px;
                        height: 20px;
                        border-radius: 50%;
                        border: 2px solid white;
                        box-shadow: 0 0 5px rgba(0,0,0,0.5);
                    }
                    .bus-stop { background-color: red; }
                    .tram-stop { background-color: green; }
                    .taxi-stop { background-color: yellow; }
                    .walking-stop { background-color: blue; }
                    .route-line { stroke-width: 3; }
                    .bus-line { stroke: red; }
                    .tram-line { stroke: green; }
                    .taxi-line { stroke: yellow; }
                    .walking-line { stroke: blue; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([40.7654, 29.9408], 13);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors'
                    }).addTo(map);
                    
                    var markers = [];
                    var lines = [];
                    
                    function clearMap() {
                        markers.forEach(marker => map.removeLayer(marker));
                        lines.forEach(line => map.removeLayer(line));
                        markers = [];
                        lines = [];
                    }
                    
                    function addStop(lat, lon, name, type, isTerminal) {
                        var stopClass = type + '-stop';
                        var marker = L.marker([lat, lon], {
                            icon: L.divIcon({
                                className: 'stop-marker ' + stopClass,
                                iconSize: [20, 20]
                            })
                        }).addTo(map);
                        
                        marker.bindPopup(
                            '<b>' + name + '</b><br>' +
                            'Tip: ' + type + '<br>' +
                            (isTerminal ? 'Terminal Durak' : '')
                        );
                        
                        markers.push(marker);
                    }
                    
                    function addConnection(fromLat, fromLon, toLat, toLon, type) {
                        var lineClass = type + '-line';
                        var line = L.polyline([[fromLat, fromLon], [toLat, toLon]], {
                            className: 'route-line ' + lineClass,
                            color: getLineColor(type),
                            weight: 3
                        }).addTo(map);
                        
                        line.bindPopup(
                            '<b>Bağlantı</b><br>' +
                            'Tip: ' + type
                        );
                        
                        lines.push(line);
                    }
                    
                    function getLineColor(type) {
                        switch(type) {
                            case 'bus': return 'red';
                            case 'tram': return 'green';
                            case 'taxi': return 'yellow';
                            case 'walking': return 'blue';
                            default: return 'gray';
                        }
                    }
                    
                    function addRoute(segments) {
                        clearMap();
                        segments.forEach(segment => {
                            var fromStop = segment.fromStop;
                            var toStop = segment.toStop;
                            
                            if (fromStop && toStop) {
                                addStop(
                                    fromStop.location.latitude,
                                    fromStop.location.longitude,
                                    fromStop.name,
                                    fromStop.type,
                                    fromStop.isSonDurak
                                );
                                
                                addStop(
                                    toStop.location.latitude,
                                    toStop.location.longitude,
                                    toStop.name,
                                    toStop.type,
                                    toStop.isSonDurak
                                );
                                
                                addConnection(
                                    fromStop.location.latitude,
                                    fromStop.location.longitude,
                                    toStop.location.latitude,
                                    toStop.location.longitude,
                                    fromStop.type
                                );
                            }
                        });
                        
                        // Haritayı rota sınırlarına göre ayarla
                        if (segments.length > 0) {
                            var bounds = [];
                            segments.forEach(segment => {
                                if (segment.fromStop) {
                                    bounds.push([
                                        segment.fromStop.location.latitude,
                                        segment.fromStop.location.longitude
                                    ]);
                                }
                                if (segment.toStop) {
                                    bounds.push([
                                        segment.toStop.location.latitude,
                                        segment.toStop.location.longitude
                                    ]);
                                }
                            });
                            map.fitBounds(bounds);
                        }
                    }
                </script>
            </body>
            </html>
            """;

        webEngine = mapView.getEngine();
        webEngine.loadContent(htmlContent);
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                displayStopsOnMap();
                displayConnectionsOnMap();
            }
        });
    }

    private void displayStopsOnMap() {
        // Haritanın yüklenmesini bekle
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                StringBuilder script = new StringBuilder();
                script.append("clearMap(); ");
                
                for (Stop stop : system.getStops()) {
                    script.append(String.format(
                        "addStop(%f, %f, '%s', '%s', %b); ",
                        stop.getLocation().getLatitude(),
                        stop.getLocation().getLongitude(),
                        stop.getName(),
                        stop.getType(),
                        stop.isSonDurak()
                    ));
                }
                
                webEngine.executeScript(script.toString());
            }
        });
    }

    private void displayConnectionsOnMap() {
        // Haritanın yüklenmesini bekle
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                StringBuilder script = new StringBuilder();
                
                for (Stop stop : system.getStops()) {
                    for (Map<String, Object> nextStop : stop.getNextStops()) {
                        String nextStopId = (String) nextStop.get("stopId");
                        Stop nextStopObj = system.getStop(nextStopId);
                        if (nextStopObj != null) {
                            script.append(String.format(
                                "addConnection(%f, %f, %f, %f, '%s'); ",
                                stop.getLocation().getLatitude(),
                                stop.getLocation().getLongitude(),
                                nextStopObj.getLocation().getLatitude(),
                                nextStopObj.getLocation().getLongitude(),
                                stop.getType()
                            ));
                        }
                    }
                }
                
                webEngine.executeScript(script.toString());
            }
        });
    }

    private void displayRoute(List<RouteSegment> route) {
        if (route == null || route.isEmpty()) {
            return;
        }

        // Haritayı temizle ve durakları göster
        displayStopsOnMap();

        // Rota segmentlerini göster
        StringBuilder script = new StringBuilder();
        script.append("addRoute(");
        script.append(new Gson().toJson(route));
        script.append(");");
        
        webEngine.executeScript(script.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
} 