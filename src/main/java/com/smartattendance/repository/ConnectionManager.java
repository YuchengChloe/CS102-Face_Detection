package com.smartattendance.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {
    // Path where the SQLite database will be stored
    // Path currently set to user's home directory under "smart-attendance/studentDB.db"
    private static final String EXTRACTED_DB_PATH = System.getProperty("user.home") + File.separator
            + "smart-attendance" + File.separator + "studentDB.db";

    // Path to the SQL script inside the JAR
    //working
    private static final String DB_RESOURCE_PATH = "/db/studentDB.sql";

    public Connection getConnection() throws SQLException {
        // Ensure the database is extracted and initialized if not already done
        File dbFile = new File(EXTRACTED_DB_PATH);
        if (!dbFile.exists()) {
            createDatabase();
        }

        // Connect to the SQLite database
        String url = "jdbc:sqlite:" + EXTRACTED_DB_PATH;
        Connection conn = DriverManager.getConnection(url);

        // Enable foreign key support (SQLite-specific)
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    // Method to create the SQLite database using the studentDB.sql script
    private void createDatabase() {
        // Ensure the directory exists
        File dir = new File(System.getProperty("user.home") + File.separator + "smart-attendance");
        if (!dir.exists()) {
            dir.mkdirs(); // Create the directory if it doesn't exist
        }

        // Create a new database file (this is where SQLite will store the data)
        File dbFile = new File(EXTRACTED_DB_PATH);

        // Try to create the database and initialize it from the SQL script
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + EXTRACTED_DB_PATH)) {
            // Read the SQL script from the JAR file
            InputStream in = getClass().getResourceAsStream(DB_RESOURCE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Database SQL script not found in JAR: " + DB_RESOURCE_PATH);
            }

            // Convert InputStream to String (SQL commands)
            String sqlScript = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            // System.out.println("Executing SQL script: \n" + sqlScript);

            // Execute the SQL script to create the tables and schema
            // if conn is successfully created, execute the script
            try (Statement stmt = conn.createStatement()) {
                String[] statements = sqlScript.split(";");
                for (String sqlcmd : statements) {
                    sqlcmd = sqlcmd.trim();
                    if (!sqlcmd.isEmpty()) {
                        stmt.execute(sqlcmd);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Error executing SQL script", e);
            }

            System.out.println("Database created and schema initialized.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing the database", e);
        }
    }
}
