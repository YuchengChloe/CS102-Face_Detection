package com.smartattendance.gui.controllers;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
// import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class MainViewController {

    @FXML
    private StackPane contentPane;

    // This will hold the controller of the currently loaded view
    private Object currentViewController;

    @FXML
    private void handleStudents() {
        loadView("StudentView.fxml");
    }

    // [NEW] Add this method
    @FXML
    private void handleLive() {
        loadView("LiveRecognitionView.fxml");
    }

    // @FXML
    // private void handleSessionsTab() {
    //     loadView("SessionView.fxml");
    // }
    // ... (other handlers) ...


    private void loadView(String viewName) {
        // [EDIT] Add shutdown logic before loading the new view
        shutdownCurrentController();

        try {
            URL fxmlLocation = getClass().getResource("/fxml/" + viewName);
            if (fxmlLocation == null) {
                throw new RuntimeException("FXML file not found: /fxml/" + viewName);
            }

            // [EDIT] Store the loader to get the controller
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent view = loader.load();

            // Store the new controller
            this.currentViewController = loader.getController();

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * [NEW] Checks if the current controller is the LiveRecognitionController
     * and calls its shutdown() method to stop the camera thread.
     */
    private void shutdownCurrentController() {
        if (this.currentViewController instanceof LiveRecognitionController) {
            try {
                ((LiveRecognitionController) this.currentViewController).shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.currentViewController = null;
    }
}