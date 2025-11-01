package com.smartattendance.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;

import com.smartattendance.model.Student;
import com.smartattendance.repository.ConnectionManager;
import com.smartattendance.repository.StudentRepositoryDAO;

import java.sql.SQLException;
import java.util.List;

public class StudentEditDialogController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> classGroupField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    
    private final StudentRepositoryDAO studentRepo;
    private Dialog<Student> dialog;
    private Student originalStudent;
    
    public StudentEditDialogController() {
        studentRepo = new StudentRepositoryDAO(new ConnectionManager());
    }
    
    @FXML
    public void initialize() {
        initializeClassGroups();
        setupValidation();
    }
    
    private void initializeClassGroups() {
        try {
            List<String> groups = studentRepo.getAllClassGroups();
            classGroupField.getItems().addAll(groups);
            
            if (groups.isEmpty()) {
                classGroupField.getItems().addAll("CS102", "CS101", "CS103");
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load class groups: " + e.getMessage());
            classGroupField.getItems().addAll("CS102", "CS101", "CS103");
        }
    }
    
    private void setupValidation() {
        // Add listeners for validation
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());
        classGroupField.valueProperty().addListener((obs, oldVal, newVal) -> validateInput());
    }
    
    public void setDialog(Dialog<Student> dialog) {
        this.dialog = dialog;
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Re-validate when dialog is set
        validateInput();
    }
    
    public void setStudent(Student student) {
        this.originalStudent = student;
        
        // Pre-populate fields with existing data
        idField.setText(student.getStudentID());
        nameField.setText(student.getStudentName());
        classGroupField.setValue(student.getClassGroup());
        emailField.setText(student.getEmail() != null ? student.getEmail() : "");
        phoneField.setText(student.getPhone() != null ? student.getPhone() : "");
        
        validateInput();
    }
    
    private void validateInput() {
        boolean isValid = !nameField.getText().trim().isEmpty() 
                      && classGroupField.getValue() != null;
                      
        if (dialog != null) {
            Node saveButton = dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst().orElse(null)
            );
            
            if (saveButton != null) {
                saveButton.setDisable(!isValid);
            }
        }
    }
    
    public Student getUpdatedStudent() {
        if (originalStudent == null) {
            return null;
        }
        
        String name = nameField.getText().trim();
        String classGroup = classGroupField.getValue();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (name.isEmpty() || classGroup == null) {
            throw new IllegalArgumentException("Name and Class Group are required");
        }
        
        // Create updated student with same ID and face data
        return new Student(
            originalStudent.getStudentID(),
            name,
            classGroup,
            email.isEmpty() ? null : email,
            phone.isEmpty() ? null : phone,
            originalStudent.getFaceData() // Keep existing face data
        );
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}