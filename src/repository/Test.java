package src.repository;
import java.sql.*;
import java.util.*;
import java.nio.file.*;

import src.FaceData;
import src.Student;

public class Test {
    // COMPILE ALL FILES BEFORE TESTING
    public static void main(String[] args) {
        // --- SETUP ---
        ConnectionManager cm = new ConnectionManager();               // your helper
        StudentRepositoryDAO dao = new StudentRepositoryDAO(cm);      // your DAO

        // test data
        String sid = "S1001";
        String imagePath = "/Users/JiaYing/Library/CloudStorage/OneDrive-SingaporeManagementUniversity/Sem 2.1/CS102/Project/CS102-Smart-Attendance-System-Project/images/testImage1.HEIC"; // <-- change this
        if (!Files.exists(Paths.get(imagePath))) {
            System.out.println("[WARN] Image path does not exist: " + imagePath);
        }

        // Start with a clean state for this sid (ignore errors if not present)
        try {
            boolean removed = dao.deleteStudent(sid);
            if (removed) System.out.println("[CLEANUP] Removed leftover student " + sid);
        } catch (SQLException ignore) {}

        // 1) ADD student + images
        try {
            FaceData fd = new FaceData(); // can be empty at creation; DAO will insert images
            Student s = new Student(
                    sid,
                    "Alice Tan",
                    "G1",
                    null,               // email is null to test setNull
                    "91234567",
                    fd
            );

            List<String> images = Arrays.asList(imagePath); // one local image
            boolean ok = dao.addStudentAndImages(s, images);
            System.out.println(ok
                    ? "[ADD] OK: student + image(s) inserted"
                    : "[ADD] FAIL: insert returned false");
        } catch (SQLException e) {
            System.out.println("[ADD] EXCEPTION:");
            e.printStackTrace();
        }

        // 2) EXISTS check
        try {
            boolean exists = dao.isStudentExists(sid);
            System.out.println(exists
                    ? "[EXISTS] YES: " + sid + " found"
                    : "[EXISTS] NO: " + sid + " not found");
        } catch (SQLException e) {
            System.out.println("[EXISTS] EXCEPTION:");
            e.printStackTrace();
        }

        // 3) GET by ID (and print)
        try {
            Student s = dao.getStudentByID(sid);
            if (s != null) {
                System.out.println("[GET] OK: " + sid);
                printStudent(s);
            } else {
                System.out.println("[GET] FAIL: " + sid + " not found");
            }
        } catch (SQLException e) {
            System.out.println("[GET] EXCEPTION:");
            e.printStackTrace();
        }

        // 4) UPDATE (set email, change class_group)
        try {
            Student s = dao.getStudentByID(sid);
            if (s == null) {
                System.out.println("[UPDATE] SKIP: student not found");
            } else {
                Student updated = new Student(
                        s.getStudentID(),
                        s.getStudentName(),
                        "G2",                    // change class
                        "alice@example.com",     // add email
                        s.getPhone(),
                        s.getFaceData()
                );
                boolean ok = dao.updateStudent(updated);
                System.out.println(ok ? "[UPDATE] OK" : "[UPDATE] FAIL");

                // re-fetch to verify
                Student after = dao.getStudentByID(sid);
                System.out.println("[VERIFY UPDATE]");
                printStudent(after);
            }
        } catch (SQLException e) {
            System.out.println("[UPDATE] EXCEPTION:");
            e.printStackTrace();
        }

        // 5) GET ALL (print count and first few)
        try {
            List<Student> all = dao.getAllStudents();
            System.out.println("[ALL] count = " + all.size());
            int shown = 0;
            for (Student s : all) {
                printStudent(s);
                shown++;
                if (shown >= 5) { // avoid spamming console
                    if (all.size() > 5) System.out.println("... (" + (all.size() - 5) + " more)");
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("[ALL] EXCEPTION:");
            e.printStackTrace();
        }

        // 6) DELETE (student + images)
        try {
            boolean ok = dao.deleteStudent(sid);
            System.out.println(ok ? "[DELETE] OK" : "[DELETE] FAIL (maybe not found)");
        } catch (SQLException e) {
            System.out.println("[DELETE] EXCEPTION:");
            e.printStackTrace();
        }

        // 7) EXISTS after DELETE (should be false)
        try {
            boolean exists = dao.isStudentExists(sid);
            System.out.println(!exists
                    ? "[POST-DELETE EXISTS] OK: " + sid + " no longer exists"
                    : "[POST-DELETE EXISTS] FAIL: " + sid + " still exists");
        } catch (SQLException e) {
            System.out.println("[POST-DELETE EXISTS] EXCEPTION:");
            e.printStackTrace();
        }

        System.out.println("\n=== TESTS DONE ===");
    }

    private static void printStudent(Student s) {
        System.out.println("  sid   = " + s.getStudentID());
        System.out.println("  name  = " + s.getStudentName());
        System.out.println("  group = " + s.getClassGroup());
        System.out.println("  email = " + s.getEmail());
        System.out.println("  phone = " + s.getPhone());

        // print face image paths if available
        if (s.getFaceData() != null && s.getFaceData().getImagePaths() != null) {
            List<String> paths = s.getFaceData().getImagePaths();
            System.out.println("  images (" + paths.size() + "):");
            for (String p : paths) {
                System.out.println("    - " + p);
            }
        } else {
            System.out.println("  images: (none or FaceData null)");
        }
    }
}
