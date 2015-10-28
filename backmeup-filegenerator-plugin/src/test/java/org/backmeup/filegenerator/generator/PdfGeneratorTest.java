package org.backmeup.filegenerator.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.backmeup.filegenerator.generator.impl.PdfGenerator;
import org.backmeup.filegenerator.generator.impl.TextGenerator;
import org.backmeup.filegenerator.generator.utils.IOUtils;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PdfGeneratorTest {
    private static File tempDir;
    
    Random random = new Random();
    TextGenerator textGenerator = new TextGenerator(100);
    
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();


    @Before
    public void setup() throws StorageException, IOException {
        tempDir = temp.newFolder();
    }

    @After
    public void tearDown() throws StorageException {
        // Junit automatically cleans up temporary storage directories 
    }
    
    @Test
    public void testGenerateText() {
        PdfGenerator pdfGenerator = new PdfGenerator("Lorem ipsum", textGenerator.getParagraphs(1000));
        InputStream is = pdfGenerator.generate();
        Assert.assertNotNull(is);
        
        File out = new File(tempDir.getAbsolutePath() + "file.pdf");
        IOUtils.saveToFile(is, out);
        
        Assert.assertTrue(out.exists());
        Assert.assertTrue(out.length() > 0);
    }
}
