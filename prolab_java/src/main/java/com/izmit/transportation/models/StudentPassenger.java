package com.izmit.transportation.models;

import com.izmit.transportation.abstracts.AbstractPassenger;
import com.izmit.transportation.models.TransportationType;

public class StudentPassenger extends AbstractPassenger {
    public StudentPassenger(String id) {
        this.id = id;
        this.type = PassengerType.STUDENT;
    }
    
    @Override
    public double getDiscountRate() {
        return 0.5; // %50 indirim
    }
    
    @Override
    public boolean isValidForTransport(TransportationType type) {
        return type != TransportationType.TAXI; // Öğrenciler taksi kullanamaz
    }
} 