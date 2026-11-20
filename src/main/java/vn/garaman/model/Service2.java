package vn.garaman.model;

import java.math.BigDecimal;

public record Service2(Long id, String code, String name, String description, String category, BigDecimal unitPrice,
        Integer estimatedDuration, Boolean active) {
}
