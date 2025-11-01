package com.smartattendance;

// [ADD THIS IMPORT]
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    // This block loads the native OpenCV library when the application starts.
    static {
        // Using Core.NATIVE_LIBRARY_NAME ensures the correct library file
        // (e.g., opencv_java480.dll on Windows) is loaded.
        Loader.load(opencv_java.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 700);
        // Add global stylesheet
        scene.getStylesheets().add(getClass().getResource("/css/Application.css").toExternalForm());
        
        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
