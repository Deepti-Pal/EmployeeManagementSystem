package com.ems.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Singleton pattern for MySQL JDBC connection management.
 * Handles connection pooling and graceful error reporting.
 */
public class DatabaseConnection {

    // ── Configure these for your local MySQL setup ──────────────────────────
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/employee_management_db";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "newpassword123";   
    private static final String DRIVER      = "com.mysql.cj.jdbc.Driver";
    // ────────────────────────────────────────────────────────────────────────

    private static Connection connection = null;

    private DatabaseConnection() {}

    /**
     * Returns a single shared Connection (Singleton).
     * Re-opens the connection if it was closed.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                connection.setAutoCommit(true);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-java.jar to the classpath.\n" + e.getMessage());
        }
        return connection;
    }

    /** Closes the shared connection (call on application exit). */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    /** Quick connectivity test – prints success or failure. */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("[DB] Connection successful! DB: " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Connection FAILED: " + e.getMessage());
            return false;
        }
    }
}
