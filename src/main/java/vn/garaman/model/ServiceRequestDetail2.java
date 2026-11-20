package vn.garaman.model;

import java.math.BigDecimal;

public record ServiceRequestDetail2(Long id, ServiceRequest2 request, Service2 service, Technician2 technician, Integer quantity, BigDecimal unitPrice, String status) {
}
