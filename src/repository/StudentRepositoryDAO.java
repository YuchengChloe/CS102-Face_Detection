package src.repository;

import java.sql.*;
import java.util.List;

import src.FaceData;
import src.Student;


public class StudentRepositoryDAO implements StudentRepository {
    private final ConnectionManager cm;

    public StudentRepositoryDAO(ConnectionManager cm) {
        this.cm = cm;
    }

    // helper method, can be reused for getAll() or "view student"
    public FaceData loadFaceData(Connection conn, String sid) throws SQLException {
        // Get face image paths
        FaceData faceData = new FaceData();
        String sql = "SELECT img_path FROM images WHERE sid = ? ORDER BY created_at";

        // Prepare a query to fetch all image file paths for this student
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sid);
            try (ResultSet rs = ps.executeQuery()) {

                // Execute the image query and loop through the results
                while (rs.next()) {
                    // add image path into the FaceData object
                    faceData.addImagePath(rs.getString("img_path"));
                }
            }
        }
        return faceData;
    }

    public Student getStudentByID(String studentID) throws SQLException {
        String sql = "SELECT sid, sname, class_group, email, phone FROM student WHERE sid = ?";

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
                    String phone = rs.getString("phone");

                    FaceData faceData = loadFaceData(conn, sid);

                    // Return a new Student object
                    return new Student(sid, name, group, email, phone, faceData);
                }
            } catch (SQLException e) {
                // undo all if fails
                conn.rollback();
                throw e;
            }

            conn.commit();
        }

        // If no student found, return null
        return null;
    }

    public boolean addStudentAndImages(Student s, List<String> imagePaths) throws SQLException{
        String addStu = "INSERT INTO student (sid, sname, class_group, email, phone) VALUES (?, ?, ?, ?, ?)";
        String addImg = "insert into images (sid, img_path) values (?, ?)";

        try (Connection conn = cm.getConnection();
            
            PreparedStatement ps1 = conn.prepareStatement(addStu);
            PreparedStatement ps2 = conn.prepareStatement(addImg)){
            
            conn.setAutoCommit(false);
            ps1.setString(1, s.getStudentID());
            ps1.setString(2, s.getStudentName());
            ps1.setString(3, s.getClassGroup());
            
            if (s.getEmail() == null){
                ps1.setNull(4, Types.VARCHAR); 
            } else {
                ps1.setString(4, s.getEmail());
            }
            
            if (s.getPhone() == null){
                ps1.setNull(5, Types.VARCHAR);
            } else {
                ps1.setString(5, s.getPhone());
            }
            
            // bundles them together and sends them to the database in a single batch for efficiency
            for (String path : imagePaths){
                ps2.setString(1, s.getStudentID());
                ps2.setString(2, path);
                ps2.addBatch();
            }
            // checks if all images are inserted successfully
            int[] batchStatus = ps2.executeBatch();
            int isAddImgOk = 1;

            for (int i : batchStatus){
                if (i == 0){
                    isAddImgOk = 0;
                    break;
                }
            }
            int isAddStuOk = ps1.executeUpdate();
            conn.commit();
            return (isAddStuOk == 1 && isAddImgOk == 1);
        }
    }

    public boolean updateStudent(Student s) throws SQLException {
        String updateStu = "UPDATE student SET sname=?, class_group=?, email=?, phone=? WHERE sid=?";

        try (Connection conn = cm.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateStu)) {
            ps.setString(1, s.getStudentName());
            ps.setString(2, s.getClassGroup());
            ps.setString(5, s.getStudentID());

            if (s.getEmail() == null){
                ps.setNull(3, Types.VARCHAR); 
            } else {
                ps.setString(4, s.getEmail());
            }
            
            if (s.getPhone() == null){
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, s.getPhone());
            }

            int isUpdateOk = ps.executeUpdate();
            conn.commit();
            return isUpdateOk == 1;
        }
    }

    public boolean deleteStudent(String studentID) throws SQLException {
        String delStu = "DELETE FROM student WHERE sid=?";
        String delImg = "Delete from images where sid =?";

        try (Connection conn = cm.getConnection();
            PreparedStatement ps1 = conn.prepareStatement(delStu);
            PreparedStatement ps2 = conn.prepareStatement(delImg)) {
            ps2.setString(1, studentID);
            int isDelImgOk = ps2.executeUpdate();

            ps1.setString(1, studentID);
            int isDelStuOk = ps1.executeUpdate();
            
            conn.commit();
            return (isDelStuOk == 1 && isDelImgOk >= 0);
        }
    }

    public boolean isStudentExists(String studentID) throws SQLException {
        String isStuExists = "SELECT 1 FROM student WHERE sid=? LIMIT 1";
        boolean exists = false;

        try (Connection conn = cm.getConnection();
            PreparedStatement ps = conn.prepareStatement(isStuExists)){
            ps.setString(1, studentID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // check if at least one row exists
                    exists = true; 
                }
            }

            conn.commit();
            return exists;
        }
    }

}