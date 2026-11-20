package vn.garaman.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Invoice2(long id, String code, ServiceRequest2 request, String invoiceStatus, String paymentStatus,
        String paymentMethod, LocalDateTime issuedAt, LocalDateTime paidAt, List<InvoiceLine2> lines) {
    public BigDecimal calculateTotal() {
        return lines.stream().map(InvoiceLine2::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
