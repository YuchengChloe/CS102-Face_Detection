package src.repository;
import java.sql.SQLException;
import src.Student;

interface StudentRepository {
    Student getStudentByID(String studentID) throws SQLException;
    void addStudent(Student student) throws SQLException;
    void updateStudent(Student student) throws SQLException;
    void deleteStudent(Student student) throws SQLException;
    boolean isStudentExists(String studentID) throws SQLException;
}
