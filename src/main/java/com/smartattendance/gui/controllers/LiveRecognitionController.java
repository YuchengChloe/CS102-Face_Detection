package com.smartattendance.gui.controllers;

import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.RecognitionResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveRecognitionController {

    @FXML
    private ImageView videoFrameView;
    @FXML
    private Label statusLabel;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private FaceRecognitionService recognitionService;
    private boolean cameraActive = false;

    private static final int CAMERA_ID = 0;
    private static final Scalar RECT_COLOR = new Scalar(0, 255, 0); // Green

    @FXML
    public void initialize() {
        try {
            this.recognitionService = new FaceRecognitionService();
            this.capture = new VideoCapture();

            // Try to open the camera
            this.capture.open(CAMERA_ID);

            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // Start a background thread to grab frames
                Runnable frameGrabber = this::grabFrame;
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS); // ~30 FPS

                Platform.runLater(() -> statusLabel.setText("Webcam feed active."));
            } else {
                Platform.runLater(() -> statusLabel.setText("Error: Could not open webcam."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
        }
    }

    /**
     * Grabs a frame from the webcam, processes it, and updates the view.
     */
    private void grabFrame() {
        if (!this.cameraActive) {
            return;
        }

        Mat frame = new Mat();
        try {
            if (this.capture.read(frame) && !frame.empty()) {
                // Perform face detection and recognition
                List<RecognitionResult> results = recognitionService.recognizeFaces(frame);

                // Draw results on the frame
                drawResults(frame, results);

                // Convert Mat to JavaFX Image
                Image fxImage = mat2Image(frame);

                // Update the ImageView on the JavaFX Application Thread
                Platform.runLater(() -> videoFrameView.setImage(fxImage));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            frame.release();
        }
    }

    /**
     * Draws bounding boxes and labels on the frame.
     */
    private void drawResults(Mat frame, List<RecognitionResult> results) {
        for (RecognitionResult res : results) {
            // Draw rectangle
            Imgproc.rectangle(frame, res.getRect().tl(), res.getRect().br(), RECT_COLOR, 2);

            // Prepare text
            String text = String.format("%s [%.1f%%]", res.getLabel(), res.getConfidence());
            Point textPos = new Point(res.getRect().x, res.getRect().y - 10);

            // Draw text
            Imgproc.putText(frame, text, textPos, Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, RECT_COLOR, 2);
        }
    }

    /**
     * Converts an OpenCV Mat object to a JavaFX Image.
     */
    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        // Encode the frame as a PNG in memory
        Imgcodecs.imencode(".png", frame, buffer);
        // Create a JavaFX Image from the in-memory bytes
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    /**
     * This method MUST be called when switching views to stop the camera thread.
     */
    public void shutdown() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Failed to stop frame grabber thread: " + e.getMessage());
            }
        }
        if (this.capture != null && this.capture.isOpened()) {
            this.capture.release();
        }
        this.cameraActive = false;
        System.out.println("LiveRecognitionController shut down.");
    }
}