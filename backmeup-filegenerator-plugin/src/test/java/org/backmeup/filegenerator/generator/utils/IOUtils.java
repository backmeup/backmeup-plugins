package org.backmeup.filegenerator.generator.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.backmeup.model.exceptions.BackMeUpException;

public final class IOUtils {
    private IOUtils() {
        // Utility classes should have private constructor
    }
    
    public static void saveToFile(InputStream inStream, String filename) {
        saveToFile(inStream, new File(filename));
    }
    
    public static void saveToFile(InputStream inStream, File outFile) {
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outFile);
            int read = 0;
            byte[] buf = new byte[1024 * 1024];
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
                    throw new BackMeUpException(e);
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    throw new BackMeUpException(e);
                }
            }
        }
    }
}
