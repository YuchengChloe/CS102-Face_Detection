package src.repository;
import java.sql.*;
import java.util.*;
import src.Student;

public class StudentRepositoryDAO implements StudentRepository{
    private ConnectionManager cm;

    public StudentRepositoryDAO(ConnectionManager cm) {
        this.cm = cm;
    }
    
    public Student getStudentByID(String studentID) throws SQLException{
        String sql = "SELECT sid, sname, class_group, email, phone FROM student WHERE sid = ?";

        try {
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    };
}
