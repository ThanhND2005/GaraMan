package vn.garaman.model;

import java.math.BigDecimal;

public record InvoicePartDetail2(Long id, Invoice2 invoice, SparePart2 sparePart, BigDecimal quantity, BigDecimal unitPrice) {
    public BigDecimal lineTotal() { return unitPrice.multiply(quantity); }
}
