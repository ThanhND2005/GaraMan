package vn.garaman.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public final class ImportOrder2 {
    private final Long id;
    private final Supplier2 supplier;
    private final WarehouseStaff2 createdBy;
    private final LocalDateTime importedAt;
    private final String status;
    private final List<ImportOrderDetail2> details = new ArrayList<>();

    public ImportOrder2(Long id,
            Supplier2 supplier,
            WarehouseStaff2 createdBy,
            LocalDateTime importedAt,
            String status) {
        this.id = id;
        this.supplier = supplier;
        this.createdBy = createdBy;
        this.importedAt = importedAt;
        this.status = status;
    }

    public Long id() { return id; }

    public Supplier2 supplier() { return supplier; }

    public WarehouseStaff2 createdBy() { return createdBy; }

    public LocalDateTime importedAt() { return importedAt; }

    public String status() { return status; }

    public List<ImportOrderDetail2> details() { return List.copyOf(details); }

    public void addImportOrderDetail(ImportOrderDetail2 value) { details.add(value); }
}
