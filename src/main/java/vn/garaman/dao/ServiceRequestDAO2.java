package vn.garaman.dao;

import vn.garaman.model.ServiceRequest2;
import java.sql.*;
import java.util.*;

public final class ServiceRequestDAO2 extends DAO2 {
    private static final String BASE = "SELECT sr.id,v.customerId,c.fullName,v.licensePlate,sr.receivedAt,sr.status FROM tblServiceRequest2 sr JOIN tblVehicle2 v ON sr.vehicleId=v.id JOIN tblCustomer2 c ON c.id=v.customerId ";

    public ServiceRequestDAO2(Connection c) {
        super(c);
    }

    public List<ServiceRequest2> findCompletedUnpaidByCustomer(long customerId) throws SQLException {
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

    public ServiceRequest2 findById(long id) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(BASE + "WHERE sr.id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    private static ServiceRequest2 map(ResultSet r) throws SQLException {
        return new ServiceRequest2(r.getLong(1), r.getLong(2), r.getString(3), r.getString(4),
                r.getTimestamp(5).toLocalDateTime(), r.getString(6));
    }
}
