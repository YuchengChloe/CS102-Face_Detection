package com.smartattendance.model;

public class Student {
    private String studentID;
    private String studentName;
    private String classGroup;
    private String email = null;
    private String phone = null;
    private FaceData faceData;
    
    

    public Student(String studentID, String studentName, String classGroup, String email, String phone,
            FaceData faceData) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.classGroup = classGroup;
        this.email = email;
        this.phone = phone;
        this.faceData = new FaceData();
    }

    public String getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassGroup() {
        return classGroup;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public FaceData getFaceData() {
        return faceData;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setClassGroup(String classGroup) {
        this.classGroup = classGroup;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    
}
