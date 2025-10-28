package com.smartattendance.service;

import org.opencv.core.Rect;

// A simple helper class to store the result of a recognition
public class RecognitionResult {
    private Rect rect;
    private String label;
    private double confidence;

    public RecognitionResult(Rect rect, String label, double confidence) {
        this.rect = rect;
        this.label = label;
        this.confidence = confidence;
    }

    public Rect getRect() {
        return rect;
    }

    public String getLabel() {
        return label;
    }

    public double getConfidence() {
        return confidence;
    }
}