package src.repository;
import java.sql.*;
import java.util.*;

import src.FaceData;
import src.Student;

public class Test {
    public static void main(String[] args) {
        ConnectionManager cm = new ConnectionManager();
        StudentRepositoryDAO dao = new StudentRepositoryDAO(cm);

        // 2) Test data
        String sid   = "S001";
        String name  = "Alice Tan";
        String group = "G1";
        String email = "alice@smu.edu.sg";
        String phone = "91234567";
        String sampleImagePath = "/Users/JiaYing/Library/CloudStorage/OneDrive-SingaporeManagementUniversity/Sem 2.1/CS102/Project/CS102-Smart-Attendance-System-Project/images/testImage1.jpg";
        
        FaceData faceData = new FaceData();
        Student s = new Student(sid, name, group, email, phone, faceData);
    }
}
