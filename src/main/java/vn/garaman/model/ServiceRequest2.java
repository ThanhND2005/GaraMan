package vn.garaman.model;

import java.time.LocalDateTime;

public record ServiceRequest2(long id, long customerId, String customerName, String licensePlate,
        LocalDateTime receivedAt, String status) {
}
