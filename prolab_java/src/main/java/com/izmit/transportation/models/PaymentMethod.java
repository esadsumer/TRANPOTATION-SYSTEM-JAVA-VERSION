package com.izmit.transportation.models;

public enum PaymentMethod {
    KENTKART("Kentkart"),
    NAKIT("Nakit"),
    KREDIKARTI("Kredi Kartı");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 