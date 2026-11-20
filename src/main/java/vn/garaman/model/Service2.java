package vn.garaman.model;

import java.math.BigDecimal;

public record Service2(long id, String code, String name, String description, String category, BigDecimal unitPrice,
        int estimatedDuration) {
}
