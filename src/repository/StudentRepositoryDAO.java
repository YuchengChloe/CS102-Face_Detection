package src.repository;

import java.sql.*;
import src.FaceData;
import src.Student;

public class StudentRepositoryDAO implements StudentRepository {
    private final ConnectionManager cm;

    public StudentRepositoryDAO(ConnectionManager cm) {
        this.cm = cm;
    }

    @Override
    public Student getStudentByID(String studentID) throws SQLException {
        final String sql = "SELECT sid, sname, class_group, email, phone FROM student WHERE sid = ?";

        // Open a database connection and prepare the SQL statement
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Replace the '?' placeholder with the actual studentID
            ps.setString(1, studentID);
            
            // Execute the query and get the results from the database
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    // Extract each column from the current row
                    String sid   = rs.getString("sid");
                    String name  = rs.getString("sname");
                    String group = rs.getString("class_group");
                    String email = rs.getString("email");
                    int phone    = rs.getInt("phone");

                    // Get face image paths
                    FaceData faceData = new FaceData();
                    String imgSql = "SELECT img_path FROM images WHERE sid = ? ORDER BY rowid";

                    // Prepare a second query to fetch all image file paths for this student
                    try (PreparedStatement psImg = conn.prepareStatement(imgSql)) {
                        psImg.setString(1, sid);

                        // Execute the image query and loop through the results
                        try (ResultSet rsImg = psImg.executeQuery()) {
                            while (rsImg.next()) {
                                String path = rsImg.getString("img_path");
                                faceData.addImagePath(path); // add image path into the FaceData object
                            }
                        }
                    }

                    // Return a new Student object
                    return new Student(sid, name, group, email, phone, faceData);
                }
            }
        }

        // If no student found, return null
        return null;
    }
}
