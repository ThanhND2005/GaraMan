package vn.garaman.model;

import java.math.BigDecimal;

public record SparePart2(long id, String code, String name, String description, String category, String unit,
        BigDecimal unitPrice, BigDecimal stockQuantity) {
}
