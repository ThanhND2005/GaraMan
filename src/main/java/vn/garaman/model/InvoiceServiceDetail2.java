package vn.garaman.model;

import java.math.BigDecimal;

public record InvoiceServiceDetail2(Long id, Invoice2 invoice, Service2 service, Integer quantity, BigDecimal unitPrice) {
    public BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
