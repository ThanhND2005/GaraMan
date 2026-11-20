package vn.garaman.model;

import java.math.BigDecimal;

public record RequestPartDetail2(Long id, ServiceRequest2 request, SparePart2 sparePart, BigDecimal quantity, BigDecimal unitPrice) {
}
