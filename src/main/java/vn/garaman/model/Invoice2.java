package vn.garaman.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public final class Invoice2 {
    private final Long id;
    private final String code;
    private final ServiceRequest2 request;
    private final SalesStaff2 createdBy;
    private final LocalDateTime issuedAt;
    private String invoiceStatus;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private SalesStaff2 receivedBy;
    private final BigDecimal receivedAmount;
    private final List<InvoiceServiceDetail2> serviceLines = new ArrayList<>();
    private final List<InvoicePartDetail2> partLines = new ArrayList<>();

    public Invoice2(Long id,
            String code,
            ServiceRequest2 request,
            SalesStaff2 createdBy,
            LocalDateTime issuedAt,
            String invoiceStatus,
            String paymentStatus,
            String paymentMethod,
            LocalDateTime paidAt,
            SalesStaff2 receivedBy,
            BigDecimal receivedAmount) {
        this.id = id;
        this.code = code;
        this.request = request;
        this.createdBy = createdBy;
        this.issuedAt = issuedAt;
        this.invoiceStatus = invoiceStatus;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.receivedBy = receivedBy;
        this.receivedAmount = receivedAmount;
    }

    public Long id() { return id; }

    public String code() { return code; }

    public ServiceRequest2 request() { return request; }

    public SalesStaff2 createdBy() { return createdBy; }

    public LocalDateTime issuedAt() { return issuedAt; }

    public String invoiceStatus() { return invoiceStatus; }

    public String paymentStatus() { return paymentStatus; }

    public String paymentMethod() { return paymentMethod; }

    public LocalDateTime paidAt() { return paidAt; }

    public SalesStaff2 receivedBy() { return receivedBy; }

    public BigDecimal receivedAmount() { return receivedAmount; }

    public List<InvoiceServiceDetail2> serviceLines() { return List.copyOf(serviceLines); }

    public void addInvoiceServiceDetail(InvoiceServiceDetail2 value) { serviceLines.add(value); }

    public List<InvoicePartDetail2> partLines() { return List.copyOf(partLines); }

    public void addInvoicePartDetail(InvoicePartDetail2 value) { partLines.add(value); }

    public List<InvoiceLine2> lines() {
        List<InvoiceLine2> result = new ArrayList<>();
        for (InvoicePartDetail2 line : partLines) {
            SparePart2 p = line.sparePart();
            result.add(new InvoiceLine2("PART", p.id(), p.code(), p.name(), line.quantity(), line.unitPrice()));
        }
        for (InvoiceServiceDetail2 line : serviceLines) {
            Service2 s = line.service();
            result.add(new InvoiceLine2("SERVICE", s.id(), s.code(), s.name(), BigDecimal.valueOf(line.quantity()), line.unitPrice()));
        }
        return List.copyOf(result);
    }

    public BigDecimal calculateTotal() {
        return java.util.stream.Stream.concat(serviceLines.stream().map(InvoiceServiceDetail2::lineTotal),
                partLines.stream().map(InvoicePartDetail2::lineTotal)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalAmount() { return calculateTotal(); }

    public void confirm() {
        if (serviceLines.isEmpty() && partLines.isEmpty()) throw new IllegalStateException("Empty invoice");
        if (!"COMPLETED".equals(request.status())) throw new IllegalStateException("Incomplete request");
        invoiceStatus = "CONFIRMED";
    }

    public void markPaid(String method, LocalDateTime time, SalesStaff2 staff) {
        if (!"CONFIRMED".equals(invoiceStatus) || !"UNPAID".equals(paymentStatus))
            throw new IllegalStateException("Invoice cannot be paid");
        if (method == null || !java.util.Set.of("CASH", "TRANSFER", "CARD").contains(method) || time == null || staff == null)
            throw new IllegalArgumentException("Invalid payment");
        paymentStatus = "PAID";
        paymentMethod = method;
        paidAt = time;
        receivedBy = staff;
    }
}
