package com.smartattendance.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;



public class PDFGenerator implements ReportGenerator {
    private ArrayList<String> students;

    public PDFGenerator() {
        this.students = new ArrayList<>();
    }

    // Added constructor that matches how the test constructs it
    public PDFGenerator(ArrayList<String> students) {
        this.students = students;
    }
    
    @Override
    public void generate() {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("example.pdf"));
            document.open();
            for (String data : this.students) {
                document.add(new Paragraph(data));
            }
            document.add(new Paragraph("Hello, World! This is a PDF document created using Java."));
             PdfPTable table = new PdfPTable(3); 
            for (int i = 0; i < 3; i++) {
                table.addCell("Column " + (i + 1));
            }
            for (int j = 0; j < 3; j++) {
                table.addCell("Row " + (j + 1));
            }
            document.add(table);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}


// public class PDFGenerator implements ReportGenerator{
//     public PDFGenerator() {
//         //constructor
//     }

//     @Override
//     public void generate(String[] args) {
//         for (String line : args) {
//                 //student ID, name, status, timestamp, confidence, method, notes 
//         }
//     }