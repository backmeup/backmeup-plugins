package org.backmeup.filegenerator.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.backmeup.filegenerator.generator.impl.BinaryGenerator;
import org.backmeup.filegenerator.generator.utils.IOUtils;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BinaryGeneratorTest {
    private static File tempDir;
    
    Random random = new Random();
    
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
        BinaryGenerator binGenerator = new BinaryGenerator(1024, random);
        InputStream is = binGenerator.generate();
        Assert.assertNotNull(is);
        
        File out = new File(tempDir.getAbsolutePath() + "file.bin");
        IOUtils.saveToFile(is, out);
        
        Assert.assertTrue(out.exists());
        Assert.assertTrue(out.length() > 0);
    }
}
