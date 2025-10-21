package com.smartattendance.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;

import com.smartattendance.model.Student;
import com.smartattendance.repository.StudentRepositoryDAO;
import com.smartattendance.repository.ConnectionManager;

import java.sql.SQLException;
import java.util.List;

public class StudentViewController {

    @FXML
    private TableView<Student> studentTable;

    @FXML
    private TableColumn<Student, String> idColumn;

    @FXML
    private TableColumn<Student, String> nameColumn;

    @FXML
    private TableColumn<Student, String> groupColumn;

    @FXML
    private TableColumn<Student, String> emailColumn;

    @FXML
    private TableColumn<Student, String> phoneColumn;

    private StudentRepositoryDAO studentRepo;

    @FXML
    public void initialize() {
        // Set up table columns (assuming Student class has JavaFX properties or getters)
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentID()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        groupColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClassGroup()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        // Setup the DAO and load data
        ConnectionManager cm = new ConnectionManager();
        studentRepo = new StudentRepositoryDAO(cm);

        loadStudents();
    }

    private void loadStudents() {
        try {
            List<Student> students = studentRepo.getAllStudents();
            ObservableList<Student> data = FXCollections.observableArrayList(students);
            studentTable.setItems(data);
        } catch (SQLException e) {
            showError("Error loading students", e.getMessage());
        }
    }

    @FXML
    private void handleAddStudent() {
        // TODO: Show a dialog to enter new student info (or load another FXML view)
        // Then call studentRepo.addStudentAndImages(...)
        showInfo("Add Student", "Add student logic goes here.");
    }

    @FXML
    private void handleDeleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                boolean deleted = studentRepo.deleteStudent(selected.getStudentID());
                if (deleted) {
                    loadStudents();
                    showInfo("Student Deleted", "Student has been successfully deleted.");
                } else {
                    showError("Deletion Failed", "Failed to delete student.");
                }
            } catch (SQLException e) {
                showError("Database Error", e.getMessage());
            }
        } else {
            showError("No Selection", "Please select a student to delete.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
