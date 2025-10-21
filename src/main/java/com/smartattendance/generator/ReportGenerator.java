package com.smartattendance.generator;

public interface ReportGenerator {
    //So imagine we will have a function that outputs student[], with list of 
    //student ID, name, status, timestamp, confidence, method, notes for each index,
    //In GUI: Filterable report view; export button with file chooser. --> generate() is bind to a button ig...
    //Optional: Email reports using JavaMail API

    //guess ill start on the GUI soon....

    //in design they say to use builder pattern, wtf is builder pattern...
    abstract void generate();
}
