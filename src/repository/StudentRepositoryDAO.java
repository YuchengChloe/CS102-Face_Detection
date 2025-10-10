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

        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String sid   = rs.getString("sid");
                    String name  = rs.getString("sname");
                    String group = rs.getString("class_group");
                    String email = rs.getString("email");
                    int phone    = rs.getInt("phone");

                    // --- Get face image paths ---
                    FaceData faceData = new FaceData();
                    String imgSql = "SELECT img_path FROM images WHERE sid = ? ORDER BY rowid";

                    try (PreparedStatement psImg = conn.prepareStatement(imgSql)) {
                        psImg.setString(1, sid);
                        try (ResultSet rsImg = psImg.executeQuery()) {
                            while (rsImg.next()) {
                                String path = rsImg.getString("img_path");
                                faceData.addImagePath(path);
                            }
                        }
                    }

                    // --- Return a new Student object ---
                    return new Student(sid, name, group, email, phone, faceData);
                }
            }
        }

        // If no student found
        return null;
    }
}
