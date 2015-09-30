package org.backmeup.filegenerator.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.backmeup.filegenerator.generator.impl.TextGenerator;
import org.backmeup.filegenerator.generator.utils.IOUtils;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TextGeneratorTest {
    private static File tempDir;
    
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
        TextGenerator textGenerator = new TextGenerator(100);
        InputStream is = textGenerator.generate();
        Assert.assertNotNull(is);
        
        File out = new File(tempDir.getAbsolutePath() + "Text.txt");
        IOUtils.saveToFile(is, out);
        
        Assert.assertTrue(out.exists());
        Assert.assertTrue(out.length() > 0);
    }
}
