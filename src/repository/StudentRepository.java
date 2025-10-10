package src.repository;
import src.Student;

interface StudentRepository {
    Student getStudentByID(String studentID);
    void addStudent(Student student);
    void updateStudent(Student student);
    void deleteStudent(Student student);
    boolean isStudentExists(String studentID);
}
