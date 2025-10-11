package src.repository;
import java.sql.SQLException;
import src.Student;

interface StudentRepository {
    Student getStudentByID(String studentID) throws SQLException;
    boolean addStudent(Student student) throws SQLException;
    boolean updateStudent(Student student) throws SQLException;
    boolean deleteStudent(String studentID) throws SQLException;
    boolean isStudentExists(String studentID) throws SQLException;
}
