module com.izmit.transportation {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.google.gson;
    requires com.sothawo.mapjfx;
    requires com.fasterxml.jackson.databind;

    opens com.izmit.transportation to javafx.fxml;
    opens com.izmit.transportation.models to javafx.fxml, com.google.gson;
    exports com.izmit.transportation;
    exports com.izmit.transportation.models;
} 