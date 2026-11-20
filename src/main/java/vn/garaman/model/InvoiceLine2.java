package vn.garaman.model;

import java.math.BigDecimal;

public record InvoiceLine2(String type, long itemId, String code, String name, BigDecimal quantity,
        BigDecimal unitPrice) {
    public BigDecimal lineTotal() {
        return quantity.multiply(unitPrice);
    }
}
