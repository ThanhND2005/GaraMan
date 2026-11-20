package vn.garaman.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public final class ServiceRequest2 {
    private final Long id;
    private final Vehicle2 vehicle;
    private final SalesStaff2 receivedBy;
    private final LocalDateTime receivedAt;
    private final String status;
    private final List<ServiceRequestDetail2> serviceDetails = new ArrayList<>();
    private final List<RequestPartDetail2> partDetails = new ArrayList<>();

    public ServiceRequest2(Long id,
            Vehicle2 vehicle,
            SalesStaff2 receivedBy,
            LocalDateTime receivedAt,
            String status) {
        this.id = id;
        this.vehicle = vehicle;
        this.receivedBy = receivedBy;
        this.receivedAt = receivedAt;
        this.status = status;
    }

    public Long id() { return id; }

    public Vehicle2 vehicle() { return vehicle; }

    public SalesStaff2 receivedBy() { return receivedBy; }

    public LocalDateTime receivedAt() { return receivedAt; }

    public String status() { return status; }

    public List<ServiceRequestDetail2> serviceDetails() { return List.copyOf(serviceDetails); }

    public void addServiceRequestDetail(ServiceRequestDetail2 value) { serviceDetails.add(value); }

    public List<RequestPartDetail2> partDetails() { return List.copyOf(partDetails); }

    public void addRequestPartDetail(RequestPartDetail2 value) { partDetails.add(value); }

    public Long customerId() { return vehicle.customer().id(); }
    public String customerName() { return vehicle.customer().fullName(); }
    public String licensePlate() { return vehicle.licensePlate(); }

    public void supplement(Service2 item, int quantity) {
        for (int i = 0; i < serviceDetails.size(); i++) {
            ServiceRequestDetail2 line = serviceDetails.get(i);
            if (line.service().id().equals(item.id())) {
                serviceDetails.set(i, new ServiceRequestDetail2(line.id(), this, item, line.technician(), Math.addExact(line.quantity(), quantity), line.unitPrice(), line.status()));
                return;
            }
        }
        serviceDetails.add(new ServiceRequestDetail2(null, this, item, null, quantity, item.unitPrice(), "COMPLETED"));
    }

    public void supplement(SparePart2 item, java.math.BigDecimal quantity) {
        for (int i = 0; i < partDetails.size(); i++) {
            RequestPartDetail2 line = partDetails.get(i);
            if (line.sparePart().id().equals(item.id())) {
                partDetails.set(i, new RequestPartDetail2(line.id(), this, item, line.quantity().add(quantity), line.unitPrice()));
                return;
            }
        }
        partDetails.add(new RequestPartDetail2(null, this, item, quantity, item.unitPrice()));
    }
}
