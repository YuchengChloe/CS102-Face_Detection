package com.smartattendance.generator;

import java.util.ArrayList;

import org.junit.Test;

import com.smartattendance.generator.CSVGenerator;
import com.smartattendance.generator.PDFGenerator;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Test
    public void TestGenerator() {
        ArrayList<String> students = new ArrayList<>();
        students.add("1, Gabriel, present, 1:30:10, 75%, method1, note1");
        students.add("2, Ugen, present, 2:40:59, 88%, method2, note1");
        students.add("3, Sime, not present, 3:56:56, 67%, method1, note3");

        PDFGenerator genpdf = new PDFGenerator(students);
        CSVGenerator gencsv = new CSVGenerator(students);

        gencsv.generate();
        genpdf.generate();
    }
}