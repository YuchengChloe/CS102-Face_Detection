package com.smartattendance;

// [ADD THIS IMPORT]
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    // [ADD THIS STATIC BLOCK]
    // This block loads the native OpenCV library when the application starts.
    static {
        // Using Core.NATIVE_LIBRARY_NAME ensures the correct library file
        // (e.g., opencv_java480.dll on Windows) is loaded.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
