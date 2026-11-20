package vn.garaman.dao;

import java.sql.*;
import java.util.TimeZone;

public abstract class DAO2 {
    static {
        // pgJDBC sends the JVM's time zone during the PostgreSQL startup handshake.
        if ("Asia/Saigon".equals(TimeZone.getDefault().getID())) {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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
