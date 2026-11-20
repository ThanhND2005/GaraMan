package vn.garaman.dao;

import vn.garaman.model.*;
import java.sql.*;
import java.util.*;

public final class CustomerDAO2 extends DAO2 {
    public CustomerDAO2(Connection c) {
        super(c);
    }

    public List<Customer2> findByKeyword(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank())
            return List.of();
        String sql = "SELECT c.*,v.id vehicleId,v.licensePlate,v.brand,v.manufactureYear FROM tblCustomer2 c LEFT JOIN tblVehicle2 v ON v.customerId=c.id WHERE c.fullName ILIKE ? ESCAPE '\\' OR EXISTS (SELECT 1 FROM tblVehicle2 x WHERE x.customerId=c.id AND x.licensePlate ILIKE ? ESCAPE '\\') ORDER BY c.fullName,c.id,v.licensePlate";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, pattern(keyword));
            p.setString(2, pattern(keyword));
            try (ResultSet r = p.executeQuery()) {
                List<Customer2> out = new ArrayList<>();
                Customer2 customer = null;
                while (r.next()) {
                    if (customer == null || customer.id() != r.getLong("id")) {
                        customer = map(r);
                        out.add(customer);
                    }
                    if (r.getObject("vehicleId") != null) customer.addVehicle(vehicle(r, customer));
                }
                return out;
            }
        }
    }

    static Customer2 map(ResultSet r) throws SQLException {
        return new Customer2(r.getLong("id"), r.getString("fullName"), r.getString("phone"), r.getString("address"), r.getString("email"));
    }

    static Vehicle2 vehicle(ResultSet r, Customer2 customer) throws SQLException {
        return new Vehicle2(r.getLong("vehicleId"), r.getString("licensePlate"), r.getString("brand"), r.getObject("manufactureYear", Integer.class), customer);
    }
}
