package vn.garaman.dao;

import vn.garaman.model.*;
import java.sql.*;
import java.util.*;

public final class ServiceRequestDAO2 extends DAO2 {
    private static final String BASE = "SELECT c.*,v.id vehicleId,v.licensePlate,v.brand,v.manufactureYear,sr.id requestId,sr.receivedById,sr.receivedAt,sr.status FROM tblServiceRequest2 sr JOIN tblVehicle2 v ON sr.vehicleId=v.id JOIN tblCustomer2 c ON c.id=v.customerId ";

    public ServiceRequestDAO2(Connection c) {
        super(c);
    }

    public List<ServiceRequest2> findCompletedUnpaidByCustomer(Long customerId) throws SQLException {
        String sql = BASE
                + "LEFT JOIN tblInvoice2 i ON i.serviceRequestId=sr.id WHERE v.customerId=? AND sr.status='COMPLETED' AND (i.id IS NULL OR i.paymentStatus='UNPAID') ORDER BY sr.receivedAt DESC,sr.id DESC";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setLong(1, customerId);
            try (ResultSet r = p.executeQuery()) {
                List<ServiceRequest2> out = new ArrayList<>();
                while (r.next())
                    out.add(map(r));
                return out;
            }
        }
    }

    public ServiceRequest2 findById(Long id) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(BASE + "WHERE sr.id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    private ServiceRequest2 map(ResultSet r) throws SQLException {
        Customer2 customer = CustomerDAO2.map(r);
        Vehicle2 vehicle = CustomerDAO2.vehicle(r, customer);
        customer.addVehicle(vehicle);
        ServiceRequest2 request = new ServiceRequest2(r.getLong("requestId"), vehicle, salesStaff(r.getLong("receivedById")),
                r.getTimestamp("receivedAt").toLocalDateTime(), r.getString("status"));
        loadDetails(request);
        return request;
    }

    SalesStaff2 salesStaff(long id) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement("SELECT * FROM tblMember2 WHERE id=? AND role='SALES_STAFF'")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Missing staff " + id);
                return new SalesStaff2(id, r.getString("username"), r.getString("passwordHash"), r.getString("fullName"),
                        r.getString("address"), r.getObject("birthDate", java.time.LocalDate.class), r.getString("email"),
                        r.getString("phone"), r.getBoolean("active"), r.getString("position"));
            }
        }
    }

    Technician2 technician(long id) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement("SELECT * FROM tblMember2 WHERE id=? AND role='TECHNICIAN'")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Missing staff " + id);
                return new Technician2(id, r.getString("username"), r.getString("passwordHash"), r.getString("fullName"),
                        r.getString("address"), r.getObject("birthDate", java.time.LocalDate.class), r.getString("email"),
                        r.getString("phone"), r.getBoolean("active"), r.getString("specialty"), r.getString("grade"));
            }
        }
    }

    private void loadDetails(ServiceRequest2 request) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT i.*, d.id detailId,d.quantity,d.unitPrice appliedPrice,d.technicianId,d.status FROM tblServiceRequestDetail2 d JOIN tblService2 i ON i.id=d.serviceId WHERE d.requestId=? ORDER BY i.code")) {
            p.setLong(1, request.id());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    request.addServiceRequestDetail(new ServiceRequestDetail2(r.getLong("detailId"), request, ServiceDAO2.map(r),
                            r.getObject("technicianId") == null ? null : technician(r.getLong("technicianId")), r.getInt("quantity"), r.getBigDecimal("appliedPrice"), r.getString("status")));
                }
            }
        }
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT i.*, d.id detailId,d.quantity,d.unitPrice appliedPrice FROM tblRequestPartDetail2 d JOIN tblSparePart2 i ON i.id=d.sparePartId WHERE d.requestId=? ORDER BY i.code")) {
            p.setLong(1, request.id());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    request.addRequestPartDetail(new RequestPartDetail2(r.getLong("detailId"), request, SparePartDAO2.map(r),
                            r.getBigDecimal("quantity"), r.getBigDecimal("appliedPrice")));
                }
            }
        }
    }

    public void saveDetails(ServiceRequest2 request) throws SQLException {
        if (connection.getAutoCommit()) throw new SQLException("Request details require a transaction");
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO tblServiceRequestDetail2(requestId,serviceId,quantity,unitPrice,technicianId,status) VALUES (?,?,?,?,?,?) "
                + "ON CONFLICT(requestId,serviceId) DO UPDATE SET quantity=EXCLUDED.quantity,unitPrice=EXCLUDED.unitPrice,technicianId=EXCLUDED.technicianId,status=EXCLUDED.status")) {
            for (ServiceRequestDetail2 line : request.serviceDetails()) {
                p.setLong(1, request.id());
                p.setLong(2, line.service().id());
                p.setInt(3, line.quantity());
                p.setBigDecimal(4, line.unitPrice());
                p.setObject(5, line.technician() == null ? null : line.technician().id(), Types.BIGINT);
                p.setString(6, line.status());
                p.addBatch();
            }
            p.executeBatch();
        }
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO tblRequestPartDetail2(requestId,sparePartId,quantity,unitPrice) VALUES (?,?,?,?) "
                + "ON CONFLICT(requestId,sparePartId) DO UPDATE SET quantity=EXCLUDED.quantity,unitPrice=EXCLUDED.unitPrice")) {
            for (RequestPartDetail2 line : request.partDetails()) {
                p.setLong(1, request.id());
                p.setLong(2, line.sparePart().id());
                p.setBigDecimal(3, line.quantity());
                p.setBigDecimal(4, line.unitPrice());
                p.addBatch();
            }
            p.executeBatch();
        }
    }
}
