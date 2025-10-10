import java.sql.*;

public class ConnectionManager {
    private static final String URL = "jdbc:sqlite:studentDB.db";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
