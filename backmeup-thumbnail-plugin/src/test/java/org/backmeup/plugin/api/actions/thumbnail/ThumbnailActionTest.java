package org.backmeup.plugin.api.actions.thumbnail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.backmeup.plugin.api.ActionException;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ThumbnailActionTest {

    private static final String TEST_JPG = "src/test/resources/creative-commons.jpg";
    private static final String TEST_PNG = "src/test/resources/creative-commons.png";
    private static final String TEST_PDF = "src/test/resources/creative-commons.pdf";

    private static Storage storage;

    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            System.out.println("PROGRESS: " + message);
        }
    };

    @Before
    public void setup() throws FileNotFoundException, StorageException {
        storage = new LocalFilesystemStorage();
        storage.open("test/originals");
        storage.addFile(new FileInputStream(TEST_JPG), "creative-commons.jpg", new MetainfoContainer());
        storage.addFile(new FileInputStream(TEST_PNG), "creative-commons.png", new MetainfoContainer());
        storage.addFile(new FileInputStream(TEST_PDF), "creative-commons.pdf", new MetainfoContainer());
    }

    @Ignore
    @Test
    public void testThumbnailAction() throws ActionException {
        ThumbnailAction t = new ThumbnailAction();
        t.doAction(null, null, storage, this.logProgressable);
    }

    @After
    public void tearDown() throws StorageException {
        storage.close();
    }

}
