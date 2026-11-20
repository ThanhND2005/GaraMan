package vn.garaman.dao;

import java.sql.*;

public abstract class DAO2 {
    protected final Connection connection;

    protected DAO2(Connection connection) {
        this.connection = connection;
    }

    public static Connection open() throws SQLException {
        String url = System.getenv().getOrDefault("GARAMAN_DB_URL", "jdbc:postgresql://localhost:5432/garaman");
        return DriverManager.getConnection(url, System.getenv().getOrDefault("GARAMAN_DB_USER", "postgres"),
                System.getenv().getOrDefault("GARAMAN_DB_PASSWORD", ""));
    }

    protected static String pattern(String keyword) {
        return "%" + keyword.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
