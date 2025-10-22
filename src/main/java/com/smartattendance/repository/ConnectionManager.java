package com.smartattendance.repository;
import java.sql.*;

public class ConnectionManager {
    private static String URL = "jdbc:sqlite:src/repository/studentDB.db";
    
    public Connection getConnection() throws SQLException {
        // 1) Open the connection
        Connection conn = DriverManager.getConnection(URL);

        // 2) Enable foreign keys for THIS connection (SQLite is per-connection)
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }
}
