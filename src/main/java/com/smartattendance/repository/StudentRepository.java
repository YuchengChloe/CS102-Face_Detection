package com.smartattendance.repository;
import java.sql.SQLException;
import java.util.List;

import com.smartattendance.model.Student;

interface StudentRepository {
    Student getStudentByID(String studentID) throws SQLException;

    boolean addStudentAndImages(Student s, List<String> imagePaths) throws SQLException;

    boolean updateStudent(Student student) throws SQLException;

    boolean deleteStudent(String studentID) throws SQLException;

    boolean isStudentExists(String studentID) throws SQLException;

    List<Student> getAllStudents() throws SQLException;

    int populateSessionRoster(int sessionID) throws SQLException;
}
