package org.backmeup.plugin.api.actions.thumbnail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.backmeup.plugin.api.ActionException;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ThumbnailActionTest {

    private static final String TEST_JPG = "src/test/resources/creative-commons.jpg";
    private static final String TEST_PNG = "src/test/resources/creative-commons.png";
    private static final String TEST_PDF = "src/test/resources/creative-commons.pdf";

    private static Storage storage;
    private static PluginContext pluginContext;
    private static File tempStorage;
    private static File tempOut;
    
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setup() throws StorageException, IOException {
        tempStorage = temp.newFolder();
        tempOut = temp.newFolder();
        
        storage = new LocalFilesystemStorage();
        storage.open(tempStorage.getPath());
        storage.addFile(new FileInputStream(TEST_JPG), "creative-commons.jpg", new MetainfoContainer());
        storage.addFile(new FileInputStream(TEST_PNG), "creative-commons.png", new MetainfoContainer());
        storage.addFile(new FileInputStream(TEST_PDF), "creative-commons.pdf", new MetainfoContainer());
        
        pluginContext = new PluginContext();
        pluginContext.setAttribute("org.backmeup.thumbnails.tmpdir", tempOut.getAbsolutePath());
    }

    @Ignore
    @Test
    public void testThumbnailAction() throws ActionException, StorageException {
        
        
        ThumbnailAction t = new ThumbnailAction();
        Assert.assertEquals(0, tempOut.listFiles().length);
        t.doAction(null, pluginContext, storage, this.logProgressable);
        Assert.assertEquals(2, tempOut.listFiles().length);
    }

    @After
    public void tearDown() throws StorageException {
        // Junit automatically cleans up temporary storage directories 
    }
    
    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            System.out.println("PROGRESS: " + message);
        }
    };

}
