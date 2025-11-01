package com.smartattendance.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.smartattendance.model.Student;
import com.smartattendance.repository.StudentRepositoryDAO;
import com.smartattendance.repository.exceptions.DuplicateStudentIDException;
import com.smartattendance.repository.exceptions.ImageProcessingException;
import com.smartattendance.repository.exceptions.StudentNotFoundException;
import com.smartattendance.repository.ConnectionManager;

import java.io.IOException;
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

    @FXML
    private TableColumn<Student, Void> actionsColumn;

    private StudentRepositoryDAO studentRepo;

    @FXML
    public void initialize() {
        // Disable row selection
        studentTable.setSelectionModel(null);

        // Set up table columns
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentID()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        groupColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClassGroup()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        // Setup action buttons column
        setupActionsColumn();

        // Setup the DAO and load data
        ConnectionManager cm = new ConnectionManager();
        studentRepo = new StudentRepositoryDAO(cm);

        loadStudents();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Student, Void>, TableCell<Student, Void>>() {
            @Override
            public TableCell<Student, Void> call(TableColumn<Student, Void> param) {
                return new TableCell<Student, Void>() {
                    private final Button editButton = createIconButton("/icons/edit.png", 16, 16);
                    private final Button deleteButton = createIconButton("/icons/delete.png", 16, 16);
                    private final HBox buttonBox = new HBox(8, editButton, deleteButton);

                    {
                        buttonBox.setAlignment(Pos.CENTER);

                        // Apply CSS style classes
                        editButton.getStyleClass().addAll("action-button", "edit-button");
                        deleteButton.getStyleClass().addAll("action-button", "delete-button");

                        // Set tooltips
                        editButton.setTooltip(new Tooltip("Edit Student"));
                        deleteButton.setTooltip(new Tooltip("Delete Student"));

                        // Event handlers
                        editButton.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            handleEditStudent(student);
                        });

                        deleteButton.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            handleDeleteStudent(student);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonBox);
                        }
                    }
                };
            }
        });
    }

    private Button createIconButton(String iconPath, int width, int height) {
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true); // Smooth scaling

            Button button = new Button();
            button.setGraphic(imageView);
            return button;
        } catch (Exception e) {
            // Fallback to text if icon not found
            System.err.println("Icon not found: " + iconPath + " - " + e.getMessage());
            Button fallbackButton = new Button(iconPath.contains("edit") ? "Edit" : "Delete");
            return fallbackButton;
        }
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
        try {
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentEnrollmentDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Create the dialog
            Dialog<Student> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Enroll New Student");

            // Get the controller and set up the dialog
            StudentEnrollmentDialogController controller = loader.getController();
            controller.setDialog(dialog);

            // Set result converter
            dialog.setResultConverter(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    return controller.createStudent();
                }
                return null;
            });

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(student -> {
                if (student != null) { // Add null check since createStudent can return null
                    try {
                        studentRepo.addStudentAndImages(student, controller.getCapturedImages());
                        loadStudents(); // Use existing loadStudents() instead of refreshStudentTable()
                        showInfo("Success",
                                String.format("Successfully enrolled %s (ID: %s) into %s",
                                        student.getStudentName(),
                                        student.getStudentID(),
                                        student.getClassGroup()));
                    } catch (DuplicateStudentIDException e) {
                        showError("Error", "Student ID already exists");
                    } catch (ImageProcessingException e) {
                        showError("Error", "Failed to process images");
                    } catch (SQLException e) {
                        showError("Database Error", "Failed to add student: " + e.getMessage());
                    }
                }
            });

        } catch (IOException e) {
            showError("Error", "Could not load enrollment dialog");
        }

    }

    @FXML
    private void handleDeleteStudent(Student student) {
        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Student");
        confirmation.setContentText(String.format(
                "Are you sure you want to delete student:\n\nID: %s\nName: %s\nClass: %s\n\nThis action cannot be undone.",
                student.getStudentID(),
                student.getStudentName(),
                student.getClassGroup()));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = studentRepo.deleteStudent(student.getStudentID());
                    if (deleted) {
                        loadStudents(); // Refresh the table
                        showInfo("Success",
                                String.format("Student %s (%s) has been successfully deleted.",
                                        student.getStudentName(),
                                        student.getStudentID()));
                    } else {
                        showError("Deletion Failed", "Failed to delete student from database.");
                    }
                } catch (StudentNotFoundException e) {
                    showError("Student Not Found", e.getMessage());
                } catch (SQLException e) {
                    showError("Database Error", "Failed to delete student: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleEditStudent(Student student) {
        try {
            // Load the edit dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentEditDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Create the dialog
            Dialog<Student> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Student - " + student.getStudentID());

            // Get the controller and set up the dialog
            StudentEditDialogController controller = loader.getController();
            controller.setDialog(dialog);
            controller.setStudent(student);

            // Set result converter
            dialog.setResultConverter(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    return controller.getUpdatedStudent();
                }
                return null;
            });

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(updatedStudent -> {
                if (updatedStudent != null) {
                    try {
                        studentRepo.updateStudent(updatedStudent);
                        loadStudents(); // Refresh the table
                        showInfo("Success",
                                String.format("Successfully updated student %s (ID: %s)",
                                        updatedStudent.getStudentName(),
                                        updatedStudent.getStudentID()));
                    } catch (StudentNotFoundException e) {
                        showError("Error", "Student not found: " + e.getMessage());
                    } catch (SQLException e) {
                        showError("Database Error", "Failed to update student: " + e.getMessage());
                    }
                }
            });

        } catch (IOException e) {
            showError("Error", "Could not load edit dialog: " + e.getMessage());
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
