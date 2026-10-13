package netcafe.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens MySQL connections for the JDBC example without storing credentials in source. */
public final class DBConnection {
    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        String url = required("NETCAFE_DB_URL");
        String user = required("NETCAFE_DB_USER");
        String password = System.getenv("NETCAFE_DB_PASSWORD");
        if (password == null) {
            throw new IllegalStateException("Set NETCAFE_DB_PASSWORD before running JdbcDemo");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Set " + name + " before running JdbcDemo");
        }
        return value;
    }
}
