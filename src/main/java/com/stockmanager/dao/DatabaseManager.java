package com.stockmanager.dao;

import com.stockmanager.model.User;
import com.stockmanager.util.PasswordUtil;

import java.sql.*;

/**
 * Manages the SQLite database connection and schema initialisation.
 * Implements the Singleton pattern to ensure a single shared connection.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:stock_manager.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initSchema();
            seedDefaultUsers();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database", e);
        }
    }

    /**
     * Package-private constructor for testing: accepts an already-open connection
     * so tests can supply an in-memory SQLite database.
     */
    DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates a {@code DatabaseManager} backed by the supplied connection.
     * Intended for use in unit tests only; do not use in production code.
     *
     * @param connection an open JDBC connection (e.g. in-memory SQLite)
     * @return a non-singleton DatabaseManager instance
     */
    public static DatabaseManager forTesting(Connection connection) {
        return new DatabaseManager(connection);
    }

    /** Returns the singleton instance, creating it if necessary. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Returns the shared database connection. */
    public Connection getConnection() {
        return connection;
    }

    /** Creates all required tables if they do not yet exist. */
    private void initSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users ("
                + "  id            INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  username      TEXT    NOT NULL UNIQUE,"
                + "  password_hash TEXT    NOT NULL,"
                + "  role          TEXT    NOT NULL CHECK(role IN ('ADMIN','USER'))"
                + ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS products ("
                + "  id         INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  name       TEXT    NOT NULL,"
                + "  quantity   INTEGER NOT NULL DEFAULT 0,"
                + "  price      REAL    NOT NULL DEFAULT 0.0,"
                + "  date_added TEXT    NOT NULL"
                + ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS transactions ("
                + "  id              INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "  product_id      INTEGER NOT NULL,"
                + "  product_name    TEXT    NOT NULL,"
                + "  action          TEXT    NOT NULL CHECK(action IN ('ADD','UPDATE','DELETE')),"
                + "  quantity_change INTEGER NOT NULL DEFAULT 0,"
                + "  date            TEXT    NOT NULL,"
                + "  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE"
                + ")"
            );
        }
    }

    /**
     * Seeds a default admin user (admin / admin123) if no users exist yet.
     * The password is BCrypt-hashed before storage.
     */
    private void seedDefaultUsers() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String hash = PasswordUtil.hashPassword("admin123");
                String insertSql =
                    "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, hash);
                    ps.setString(3, User.Role.ADMIN.name());
                    ps.executeUpdate();
                }

                // Also seed a default read-only user
                String userHash = PasswordUtil.hashPassword("user123");
                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setString(1, "user");
                    ps.setString(2, userHash);
                    ps.setString(3, User.Role.USER.name());
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Closes the database connection (call on application shutdown). */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // best-effort close
            }
        }
        instance = null;
    }
}
