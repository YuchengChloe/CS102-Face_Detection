package com.smartattendance.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CSVGenerator implements ReportGenerator {
    private ArrayList<String> students;

    public CSVGenerator() {
        this.students = new ArrayList<>();
    }

    // Added constructor that matches how the test constructs it
    public CSVGenerator(ArrayList<String> students) {
        this.students = students;
    }
    
    @Override
    public void generate() {
        CSVGenerator generator = new CSVGenerator(this.students);

        try {
            generator.givenDataArray_whenConvertToCSV_thenOutputCreated(this.students.toArray(new String[0]));
        } catch (IOException e) {
            System.out.println("IO exception: " + e);
        }
    }

    public String escapeSpecialCharacters(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
    
    public String convertToCSV(String data) {
        return Stream.of(data)
        .map(this::escapeSpecialCharacters)
        .collect(Collectors.joining(","));
    }

    public static void assertTrue(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError("Assertion failed: " + message);
            }
        }

    public void givenDataArray_whenConvertToCSV_thenOutputCreated(String[] studentData) throws IOException {
        File csvOutputFile = new File("./test.csv");   
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            Stream.of(studentData)
            .map(this::convertToCSV)
            .forEach(pw::println);
        }
        assertTrue(csvOutputFile.exists(), "csv is not created");
    }
}