package vn.garaman.model;

import java.time.LocalDateTime;

public record Appointment2(Long id, Customer2 customer, Vehicle2 vehicle, Service2 service, LocalDateTime appointmentAt, String status, String note) {
    public Appointment2 {
        if (!customer.id().equals(vehicle.customer().id())) throw new IllegalArgumentException("Vehicle owner mismatch");
    }
}
