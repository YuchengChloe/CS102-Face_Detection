package com.smartattendance.service;

import com.smartattendance.model.Student;
import com.smartattendance.repository.ConnectionManager;
import com.smartattendance.repository.StudentRepositoryDAO;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceRecognitionService {

    private static final String CASCADE_FILE_PATH = "/classifiers/haarcascade_frontalface_default.xml";
    private static final Size TRAINING_IMAGE_SIZE = new Size(200, 200);
    // Confidence threshold from demo. 0.7 for correlation is a good starting point.
    private static final double RECOGNITION_THRESHOLD = 0.7;

    private final CascadeClassifier faceDetector;
    private final StudentRepositoryDAO studentRepo;

    // Key: StudentID, Value: List of histograms for that student's images
    private final Map<String, List<Mat>> trainingData;
    // Key: StudentID, Value: Student Name
    private final Map<String, String> studentIdToNameMap;

    public FaceRecognitionService() {
        this.studentRepo = new StudentRepositoryDAO(new ConnectionManager());
        this.trainingData = new HashMap<>();
        this.studentIdToNameMap = new HashMap<>();

        // Load the face detector
        URL res = getClass().getResource(CASCADE_FILE_PATH);
        if (res == null) {
            throw new RuntimeException("Failed to find cascade file: " + CASCADE_FILE_PATH);
        }
        try {
            File cascadeFile = new File(res.toURI());
            this.faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (this.faceDetector.empty()) {
                throw new RuntimeException("Failed to load cascade classifier: " + CASCADE_FILE_PATH);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load cascade file", e);
        }

        // Load and "train" on student data
        trainModel();
    }

    /**
     * Loads all students and their face images from the database,
     * computes histograms, and stores them in memory for recognition.
     */
    private void trainModel() {
        System.out.println("Training face recognition model...");
        try {
            List<Student> students = studentRepo.getAllStudents();
            if (students.isEmpty()) {
                System.out.println("No students found in the database to train on.");
                return;
            }

            for (Student student : students) {
                studentIdToNameMap.put(student.getStudentID(), student.getStudentName());
                List<Mat> histograms = new ArrayList<>();

                if (student.getFaceData() == null || student.getFaceData().getImagePaths().isEmpty()) {
                    System.out.println("No images found for student: " + student.getStudentName());
                    continue;
                }

                for (String imgPath : student.getFaceData().getImagePaths()) {
                    Mat img = loadAndPreprocessImage(imgPath);
                    if (img != null && !img.empty()) {
                        histograms.add(computeHistogram(img));
                    }
                }
                trainingData.put(student.getStudentID(), histograms);
                System.out.println("Loaded " + histograms.size() + " images for " + student.getStudentName());
            }
            System.out.println("Model training complete. Ready for recognition.");
        } catch (SQLException e) {
            System.err.println("Database error loading training data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads an image from the resources folder, converts to grayscale, and resizes.
     */
    private Mat loadAndPreprocessImage(String resourcePath) {
        try {
            URL res = getClass().getResource("/" + resourcePath);
            if (res == null) {
                System.err.println("Failed to find resource: /" + resourcePath);
                return null;
            }
            File imgFile = new File(res.toURI());
            Mat img = Imgcodecs.imread(imgFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
            if (img.empty()) {
                System.err.println("Failed to load image: " + resourcePath);
                return null;
            }

            // Preprocess: resize to standard size
            Imgproc.resize(img, img, TRAINING_IMAGE_SIZE);
            return img;
        } catch (Exception e) {
            System.err.println("Error loading image " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Computes a normalized histogram for a given grayscale image.
     * (Copied from FaceRecognitionDemo.java)
     */
    private Mat computeHistogram(Mat image) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);
        Imgproc.calcHist(List.of(image), channels, new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        return hist;
    }

    /**
     * Finds the best histogram comparison score against a list of stored histograms.
     * (Copied from FaceRecognitionDemo.java)
     */
    private double getBestHistogramScore(Mat faceHist, List<Mat> histograms) {
        double bestScore = 0;
        for (Mat hist : histograms) {
            // HISTCMP_CORREL: Higher score = better match
            double score = Imgproc.compareHist(faceHist, hist, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }

    /**
     * Main method to be called by the controller.
     * Detects faces in a frame and attempts to recognize them.
     */
    public List<RecognitionResult> recognizeFaces(Mat frame) {
        List<RecognitionResult> results = new ArrayList<>();
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        // Detect faces
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size());

        for (Rect rect : faces.toArray()) {
            // Preprocess the detected face
            Mat face = gray.submat(rect);
            Imgproc.resize(face, face, TRAINING_IMAGE_SIZE);
            Mat faceHist = computeHistogram(face);

            // Compare with training data
            String bestMatchID = "Unknown";
            double bestScore = 0.0;

            for (Map.Entry<String, List<Mat>> entry : trainingData.entrySet()) {
                double score = getBestHistogramScore(faceHist, entry.getValue());
                if (score > bestScore) {
                    bestScore = score;
                    bestMatchID = entry.getKey();
                }
            }

            // Create result
            String label;
            double confidence;
            if (bestScore > RECOGNITION_THRESHOLD) {
                String name = studentIdToNameMap.getOrDefault(bestMatchID, "Error");
                label = name + " (" + bestMatchID + ")";
                confidence = bestScore * 100.0; // Convert 0.7-1.0 scale to 70-100%
            } else {
                label = "Unknown";
                confidence = (1.0 - bestScore) * 100.0;
            }
            results.add(new RecognitionResult(rect, label, confidence));

            // Release temp mats
            face.release();
            faceHist.release();
        }

        // Release temp mats
        gray.release();
        faces.release();

        return results;
    }
}