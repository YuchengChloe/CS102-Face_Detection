package com.smartattendance;

import org.opencv.core.Core;

/**
 * This Launcher class is the proper entry point for the application,
 * ensuring all necessary libraries are loaded correctly.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}