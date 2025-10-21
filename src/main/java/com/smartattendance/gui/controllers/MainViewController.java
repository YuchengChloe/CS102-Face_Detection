package com.smartattendance.gui.controllers;

import java.io.IOException;

import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class MainViewController {

    @FXML
    private StackPane contentPane;

    @FXML
    private void handleStudents() {
        loadView("StudentView.fxml");
    }

    // @FXML
    // private void handleSessionsTab() {
    //     loadView("SessionView.fxml");
    // }

    // @FXML
    // private void handleRecognitionTab() {
    //     loadView("RecognitionView.fxml");
    // }

    // @FXML
    // private void handleReportsTab() {
    //     loadView("ReportView.fxml");
    // }

    // @FXML
    // private void handleSettingsTab() {
    //     loadView("SettingsView.fxml");
    // }

private void loadView(String viewName) {
    try {
        URL fxmlLocation = getClass().getResource("/fxml/" + viewName);
        if (fxmlLocation == null) {
            throw new RuntimeException("FXML file not found: /fxml/" + viewName);
        }

        Parent view = FXMLLoader.load(fxmlLocation);
        contentPane.getChildren().setAll(view);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
