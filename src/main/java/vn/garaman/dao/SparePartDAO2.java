package vn.garaman.dao;

import vn.garaman.model.SparePart2;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public final class SparePartDAO2 extends DAO2 {
    public SparePartDAO2(Connection c) {
        super(c);
    }

    public List<SparePart2> findByKeyword(String keyword) throws SQLException {
        return search(keyword, false);
    }

    public List<SparePart2> findForInvoice(String keyword) throws SQLException {
        return search(keyword, true);
    }

    private List<SparePart2> search(String keyword, boolean includeCode) throws SQLException {
        if (keyword == null || keyword.isBlank())
            return List.of();
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT * FROM tblSparePart2 WHERE active AND (name ILIKE ? ESCAPE '\\'" + (includeCode ? " OR code ILIKE ? ESCAPE '\\'" : "") + ") ORDER BY name,id")) {
            p.setString(1, pattern(keyword));
            if (includeCode) p.setString(2, pattern(keyword));
            try (ResultSet r = p.executeQuery()) {
                List<SparePart2> out = new ArrayList<>();
                while (r.next())
                    out.add(map(r));
                return out;
            }
        }
    }

    public SparePart2 findById(Long id) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement("SELECT * FROM tblSparePart2 WHERE id=? AND active")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public int decreaseStockIfAvailable(Long id, BigDecimal quantity) throws SQLException {
        if (quantity == null || quantity.signum() <= 0 || quantity.stripTrailingZeros().scale() > 3) return 0;
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE tblSparePart2 SET stockQuantity=stockQuantity-? WHERE id=? AND active AND stockQuantity>=?")) {
            p.setBigDecimal(1, quantity);
            p.setLong(2, id);
            p.setBigDecimal(3, quantity);
            return p.executeUpdate();
        }
    }

    static SparePart2 map(ResultSet r) throws SQLException {
        return new SparePart2(r.getLong("id"), r.getString("code"), r.getString("name"), r.getString("description"),
                r.getString("category"), r.getString("unit"), r.getBigDecimal("unitPrice"),
                r.getBigDecimal("stockQuantity"), r.getBoolean("active"));
    }
}
