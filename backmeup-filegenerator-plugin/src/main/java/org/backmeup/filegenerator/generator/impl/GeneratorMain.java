package org.backmeup.filegenerator.generator.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class GeneratorMain {
    private static void saveToFile(InputStream inStream, String filename) {
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(filename));
            int read = 0;
            byte buf[] = new byte[1024 * 1024];
            while ((read = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, read);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        TextGenerator textGenerator = new TextGenerator(100);
        System.out.print("Generate text ... ");
        InputStream is = textGenerator.generate();
        saveToFile(is, "file1.txt");
        System.out.println("done.");

        System.out.print("Generate image ... ");
        ImageGenerator imageGenerator = new ImageGenerator(100, 100, random);
        is = imageGenerator.generate();
        saveToFile(is, "file2.jpg");
        System.out.println("done.");

        System.out.print("Generate pdf ... ");
        PdfGenerator pdfGenerator = new PdfGenerator("Lorem ipsum", textGenerator.getParagraphs(1000));
        is = pdfGenerator.generate();
        saveToFile(is, "file3.pdf");
        System.out.println("done.");

        System.out.print("Generate binary ... ");
        BinaryGenerator binGenerator = new BinaryGenerator(1024, random);
        is = binGenerator.generate();
        saveToFile(is, "file4.bin");
        System.out.println("done.");
    }
}
