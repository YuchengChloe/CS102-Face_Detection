package src.repository;

import java.sql.*;
import java.util.*;

import src.FaceData;
import src.Student;


public class StudentRepositoryDAO implements StudentRepository {
    private final ConnectionManager cm;

    public StudentRepositoryDAO(ConnectionManager cm) {
        this.cm = cm;
    }

    /*
     * caller must instatiate the ConnectionManager helper in main
     * ConnectionManager cm = new ConnectionManager();
     * StudentRepositoryDAO dao = new StudentRepositoryDAO(cm);
     */

    // helper method, can be reused for getAll() or "view student"
    public FaceData loadFaceData(Connection conn, String sid) throws SQLException {
        // Get face image paths
        FaceData faceData = new FaceData();
        String sql = "SELECT img_path FROM images WHERE sid = ? ORDER BY created_at";

        PreparedStatement ps = null; // An object that represents a precompiled SQL statement
        ResultSet rs = null; // ResultSet holds the results of an SQL SELECT statement.

        // Prepare a query to fetch all image file paths for this student
        try {
            ps = conn.prepareStatement(sql); // same as $stmt = $pdo->prepare($sql); turns SQL query into an executable command.
            ps.setString(1, sid); // same as $stmt->bindParam(':isbn', $isbn); fills the ? with the actual sid
            rs = ps.executeQuery(); // $result = $stmt->execute();
            while (rs.next()) { // fetch
                faceData.addImagePath(rs.getString("img_path")); // reads the img_path column
            }
            return faceData;
        } finally {
            if (rs != null) { // calling a method on null throws a NullPointerException thats why must check
                try {
                    rs.close();
                } catch (SQLException ignored) {}
            }

            if (ps != null){
                try {
                    ps.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public Student getStudentByID(String studentID) throws SQLException {
        String sql = "SELECT sid, sname, class_group, email, phone FROM student WHERE sid = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Open a database connection and prepare the SQL statement
            conn = cm.getConnection(); // ask the ConnectionManager object (cm) to open and return a ready-to-use database connection, then store it in the variable conn
            ps = conn.prepareStatement(sql);
            ps.setString(1, studentID); // Replace the '?' placeholder with the actual studentID
            rs = ps.executeQuery(); // execute the query and get the results from the database

            while (rs.next()) {
                String sid   = rs.getString("sid");
                String name  = rs.getString("sname");
                String group = rs.getString("class_group");
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                FaceData faceData = loadFaceData(conn, sid);

                // Return a new Student object
                return new Student(sid, name, group, email, phone, faceData);
            }
            // If no student found, return null
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored){}
            }

            if (ps != null){
                try {
                    ps.close();
                } catch (SQLException ignored) {}
            }

            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean addStudentAndImages(Student s, List<String> imagePaths) throws SQLException{
        String addStu = "INSERT INTO student (sid, sname, class_group, email, phone) VALUES (?, ?, ?, ?, ?)";
        String addImg = "insert into images (sid, img_path) values (?, ?)";

        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try {
            conn = cm.getConnection();
            conn.setAutoCommit(false); // turn off auto-saving so I can manually commit or roll back multiple SQL operations as one transaction

            ps1 = conn.prepareStatement(addStu);
            ps1.setString(1, s.getStudentID());
            ps1.setString(2, s.getStudentName());
            ps1.setString(3, s.getClassGroup());

            if (s.getEmail() == null){
                ps1.setNull(4, java.sql.Types.VARCHAR); 
            } else {
                ps1.setString(4, s.getEmail());
            }

            if (s.getPhone() == null){
                ps1.setNull(5, java.sql.Types.VARCHAR); 
            } else {
                ps1.setString(5, s.getPhone());
            }

            boolean isAddStuOk = (ps1.executeUpdate() == 1);
            boolean isAddImgOk = true;

            if (imagePaths != null && !imagePaths.isEmpty()) {
                ps2 = conn.prepareStatement(addImg);
                for (String path : imagePaths) {
                    ps2.setString(1, s.getStudentID());
                    ps2.setString(2, path);
                    ps2.addBatch(); // bundles them together and sends them to the database in a single batch for efficiency
                }
                int[] batchStatus = ps2.executeBatch();
                // scans the returned statuses and sets isAddImgOk = false if any item wasn’t successful
                for (int i : batchStatus) {
                    if (i != 1 && i != Statement.SUCCESS_NO_INFO) { // conservative check, some drivers don’t report exact counts; this still counts as success.
                        isAddImgOk = false;
                        break;
                    }
                }
            }

            if (isAddStuOk && isAddImgOk) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // something failed, undo all
                } catch (SQLException r) {
                    e.addSuppressed(r);
                }
            }
            throw e;
        } finally {
            if (ps2 != null) {
                try { 
                    ps2.close(); 
                } catch (SQLException ignored) {}
            }

            if (ps1 != null) {
                try {
                    ps1.close();
                } catch (SQLException ignored) {}
            }

            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
                
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateStudent(Student s) throws SQLException {
        String updateStu = "UPDATE student SET sname=?, class_group=?, email=?, phone=? WHERE sid=?";

        try (Connection conn = cm.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateStu)) {
            conn.setAutoCommit(false);

            ps.setString(1, s.getStudentName());
            ps.setString(2, s.getClassGroup());

            if (s.getEmail() == null){
                ps.setNull(3, Types.VARCHAR); 
            } else {
                ps.setString(3, s.getEmail());
            }
            
            if (s.getPhone() == null){
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, s.getPhone());
            }

            ps.setString(5, s.getStudentID());

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

            return (isDelStuOk == 1 && isDelImgOk >= 0);
        }
    }

    public boolean isStudentExists(String studentID) throws SQLException {
        String isStuExists = "SELECT 1 FROM student WHERE sid=? LIMIT 1";
        boolean exists = false;

        try (Connection conn = cm.getConnection();
            PreparedStatement ps = conn.prepareStatement(isStuExists)){
            conn.setAutoCommit(false);

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

    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT sid, sname, class_group, email, phone FROM student ORDER BY sid";
        List<Student> students = new ArrayList<>();

        try (Connection conn = cm.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String sid   = rs.getString("sid");
                String name  = rs.getString("sname");
                String group = rs.getString("class_group");
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                FaceData faceData = loadFaceData(conn, sid);

                Student s = new Student(sid, name, group, email, phone, faceData);
                students.add(s);
            }
        }
        return students;
    }
}