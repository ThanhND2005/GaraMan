package vn.garaman.dao;

import vn.garaman.model.Customer2;
import java.sql.*;
import java.util.*;

public final class CustomerDAO2 extends DAO2 {
    public CustomerDAO2(Connection c) {
        super(c);
    }

    public List<Customer2> findByKeyword(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank())
            return List.of();
        String sql = "SELECT c.id,c.fullName,c.phone,COALESCE(string_agg(v.licensePlate, ', ' ORDER BY v.licensePlate),'') plates FROM tblCustomer2 c LEFT JOIN tblVehicle2 v ON v.customerId=c.id GROUP BY c.id,c.fullName,c.phone HAVING c.fullName ILIKE ? ESCAPE '\\' OR EXISTS (SELECT 1 FROM tblVehicle2 x WHERE x.customerId=c.id AND x.licensePlate ILIKE ? ESCAPE '\\') ORDER BY c.fullName,c.id";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, pattern(keyword));
            p.setString(2, pattern(keyword));
            try (ResultSet r = p.executeQuery()) {
                List<Customer2> out = new ArrayList<>();
                while (r.next())
                    out.add(new Customer2(r.getLong(1), r.getString(2), r.getString(3), r.getString(4)));
                return out;
            }
        }
    }
}
