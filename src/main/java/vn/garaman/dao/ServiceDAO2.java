package vn.garaman.dao;

import vn.garaman.model.Service2;
import java.sql.*;
import java.util.*;

public final class ServiceDAO2 extends DAO2 {
    public ServiceDAO2(Connection c) {
        super(c);
    }

    public List<Service2> findByKeyword(String keyword) throws SQLException {
        return search(keyword, false);
    }

    public List<Service2> findForInvoice(String keyword) throws SQLException {
        return search(keyword, true);
    }

    private List<Service2> search(String keyword, boolean includeCode) throws SQLException {
        if (keyword == null || keyword.isBlank())
            return List.of();
        String sql = "SELECT * FROM tblService2 WHERE active AND (name ILIKE ? ESCAPE '\\'" + (includeCode ? " OR code ILIKE ? ESCAPE '\\'" : "") + ") ORDER BY name,id";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, pattern(keyword));
            if (includeCode) p.setString(2, pattern(keyword));
            try (ResultSet r = p.executeQuery()) {
                List<Service2> out = new ArrayList<>();
                while (r.next())
                    out.add(map(r));
                return out;
            }
        }
    }

    public Service2 findById(Long id) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement("SELECT * FROM tblService2 WHERE id=? AND active")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    static Service2 map(ResultSet r) throws SQLException {
        return new Service2(r.getLong("id"), r.getString("code"), r.getString("name"), r.getString("description"),
                r.getString("category"), r.getBigDecimal("unitPrice"), r.getInt("estimatedDuration"), r.getBoolean("active"));
    }
}
