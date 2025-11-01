package com.smartattendance.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

import com.smartattendance.model.Student;
import com.smartattendance.model.FaceData;
import com.smartattendance.repository.ConnectionManager;
import com.smartattendance.repository.StudentRepositoryDAO;
import com.smartattendance.repository.exceptions.DuplicateStudentIDException;
import com.smartattendance.repository.exceptions.ImageProcessingException;

public class StudentEnrollmentDialogController {
    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> classGroupField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ImageView previewArea;
    @FXML
    private Label imageCounter;
    @FXML
    private Button captureButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button stopCaptureButton;

    private final List<String> capturedImages = new ArrayList<>();
    private final StudentRepositoryDAO studentRepo;
    private Dialog<Student> dialog;
    private static final String BASE_IMAGE_PATH = "src/main/resources/images/students/";
    private static final String RELATIVE_IMAGE_PATH = "/images/students/";
    private static final String CASCADE_FILE_PATH = "/classifiers/haarcascade_frontalface_default.xml";

    private VideoCapture camera;
    private boolean isCapturing = false;
    private CascadeClassifier faceDetector;

    public StudentEnrollmentDialogController() {
        studentRepo = new StudentRepositoryDAO(new ConnectionManager());
    }

    @FXML
    public void initialize() {
        setupValidation();
        initializeClassGroups();
        loadFaceDetector();
        stopCaptureButton.setVisible(false);
    }

    private void loadFaceDetector() {
        try {
            URL res = getClass().getResource(CASCADE_FILE_PATH);
            if (res == null) {
                throw new RuntimeException("Failed to find cascade file: " + CASCADE_FILE_PATH);
            }

            File cascadeFile = new File(res.toURI());
            this.faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

            if (this.faceDetector.empty()) {
                throw new RuntimeException("Failed to load cascade classifier: " + CASCADE_FILE_PATH);
            }
        } catch (URISyntaxException e) {
            showError("Error", "Failed to load face detector: " + e.getMessage());
        }
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
        validateInput();
    }

    public void setDialog(Dialog<Student> dialog) {
        this.dialog = dialog;
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        idField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());
        classGroupField.valueProperty().addListener((obs, oldVal, newVal) -> validateInput());
    }

    @FXML
    private void handleCapture() {
        String studentID = idField.getText().trim();
        if (studentID.isEmpty()) {
            showError("Validation Error", "Please enter Student ID before capturing images");
            return;
        }

        if (isCapturing) {
            return; // Already capturing
        }

        isCapturing = true;
        captureButton.setDisable(true);
        uploadButton.setDisable(true);
        stopCaptureButton.setVisible(true);

        Task<Void> captureTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                camera = new VideoCapture(0);
                if (!camera.isOpened()) {
                    Platform.runLater(() -> showError("Camera Error", "Failed to open camera"));
                    return null;
                }

                Mat frame = new Mat();
                while (isCapturing && capturedImages.size() < 20) {
                    camera.read(frame);
                    if (frame.empty())
                        continue;

                    // Detect face in frame
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(grayFrame, faces);

                    // Only capture if exactly one face is detected
                    if (faces.toArray().length == 1) {
                        String imagePath = saveImage(frame, studentID);
                        if (imagePath != null) {
                            capturedImages.add(imagePath);
                            Platform.runLater(() -> {
                                updatePreview(imagePath);
                                updateImageCounter();
                            });
                            Thread.sleep(1000); // Wait 1 second between captures
                        }
                    }

                    Thread.sleep(100); // Control frame rate
                }

                camera.release();
                return null;
            }

            @Override
            protected void succeeded() {
                stopCapture();
            }

            @Override
            protected void failed() {
                stopCapture();
                showError("Capture Error", "Failed to capture images");
            }
        };

        new Thread(captureTask).start();
    }

    @FXML
    private void handleStopCapture() {
        stopCapture();
    }

    private void stopCapture() {
        isCapturing = false;
        captureButton.setDisable(false);
        uploadButton.setDisable(false);
        stopCaptureButton.setVisible(false);
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    @FXML
    private void handleUpload() {
        String studentID = idField.getText().trim();
        if (studentID.isEmpty()) {
            showError("Validation Error", "Please enter Student ID before uploading images");
            return;
        }

        if (capturedImages.size() >= 20) {
            showError("Limit Reached", "Maximum 20 images allowed");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Student Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dialog.getOwner());
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            int remainingSlots = 20 - capturedImages.size();
            int filesToProcess = Math.min(selectedFiles.size(), remainingSlots);

            for (int i = 0; i < filesToProcess; i++) {
                File file = selectedFiles.get(i);
                try {
                    String imagePath = copyImageToStudentFolder(file, studentID);
                    capturedImages.add(imagePath);
                    updatePreview(imagePath);
                } catch (IOException e) {
                    showError("Upload Error", "Failed to upload image: " + file.getName());
                }
            }

            updateImageCounter();
        }
    }

    private String saveImage(Mat frame, String studentID) {
        try {
            // Create student-specific folder using full path
            String studentFolder = BASE_IMAGE_PATH + studentID + "/";
            File folder = new File(studentFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Generate filename
            String fileName = String.format("%s_%d.jpg", studentID, capturedImages.size() + 1);
            String fullPath = studentFolder + fileName;

            // Save the image
            Imgcodecs.imwrite(fullPath, frame);

            // Return relative path with leading slash
            return RELATIVE_IMAGE_PATH + studentID + "/" + fileName;

        } catch (Exception e) {
            showError("Save Error", "Failed to save captured image");
            return null;
        }
    }

    private String copyImageToStudentFolder(File sourceFile, String studentID) throws IOException {
        // Create student-specific folder using full path
        String studentFolder = BASE_IMAGE_PATH + studentID + "/";
        Path folderPath = Paths.get(studentFolder);
        Files.createDirectories(folderPath);

        // Generate filename
        String fileName = String.format("%s_%d_%s", studentID, capturedImages.size() + 1, sourceFile.getName());
        Path destinationPath = folderPath.resolve(fileName);

        // Copy file
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path with leading slash
        return RELATIVE_IMAGE_PATH + studentID + "/" + fileName;
    }

    private void updatePreview(String imagePath) {
        try {
            // Remove leading slash for file path construction
            String cleanPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            String fullPath = "file:" + System.getProperty("user.dir") + "/src/main/resources/" + cleanPath;
            Image image = new Image(fullPath);
            previewArea.setImage(image);
        } catch (Exception e) {
            showError("Preview Error", "Failed to load image preview: " + e.getMessage());
        }
    }

    private void updateImageCounter() {
        imageCounter.setText(String.format("%d/20 images", capturedImages.size()));
        validateInput();
    }

    private void validateInput() {
        boolean isValid = !idField.getText().trim().isEmpty()
                && !nameField.getText().trim().isEmpty()
                && classGroupField.getValue() != null
                && capturedImages.size() >= 10;

        if (dialog != null) {
            Node saveButton = dialog.getDialogPane().lookupButton(
                    dialog.getDialogPane().getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst().orElse(null));

            if (saveButton != null) {
                saveButton.setDisable(!isValid);
            }
        }
    }

    public Student createStudent() {
        String id = idField.getText().trim();
        try {
            if (studentRepo.isStudentExists(id)) {
                throw new DuplicateStudentIDException("Cannot create student: ID " + id + " already exists");
            }

            String name = nameField.getText().trim();
            String classGroup = classGroupField.getValue();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || classGroup == null) {
                throw new IllegalArgumentException("Required fields cannot be empty");
            }

            if (capturedImages.size() < 10) {
                throw new ImageProcessingException("Minimum 10 face images required");
            }

            FaceData faceData = new FaceData();
            capturedImages.forEach(faceData::addImagePath);

            return new Student(id, name, classGroup, email, phone, faceData);

        } catch (DuplicateStudentIDException e) {
            showError("Duplicate ID", e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException("Database error while checking student ID: " + e.getMessage());
        } catch (ImageProcessingException e) {
            showError("Image Capture Error", e.getMessage());
        }
        return null;
    }

    public List<String> getCapturedImages() {
        return List.copyOf(capturedImages);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}