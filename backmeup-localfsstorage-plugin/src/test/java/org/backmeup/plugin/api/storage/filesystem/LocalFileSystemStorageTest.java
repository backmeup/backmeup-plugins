package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.filters.StringInputStream;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LocalFileSystemStorageTest {

    private static final String ROOT_PATH = "unit-test";

    private static final String TEST_TXT_1 = "Hello World! #1";

    private static final String TEST_TXT_2 = "Hello World! #2";

    @Test
    public void testOpen() throws StorageException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);

        // Directory should exist after open
        File rootPath = new File(ROOT_PATH);
        Assert.assertTrue(rootPath.exists());
        Assert.assertTrue(rootPath.isDirectory());
    }

    @Test
    public void testAddFile() throws StorageException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);

        // Test exists path methdo (negative)
        Assert.assertFalse(storage.existsPath("/mydirectory"));

        // Add two test files
        storage.addFile(new StringInputStream(TEST_TXT_1), "/hello1.txt", new MetainfoContainer());
        storage.addFile(new StringInputStream(TEST_TXT_2), "/mydirectory/hello2.txt", new MetainfoContainer());

        File rootPath = new File(ROOT_PATH);
        File file1 = new File(rootPath, "hello1.txt");
        File meta1 = new File(rootPath, "hello1.txt.meta.json");
        File file2 = new File(new File(rootPath, "mydirectory"), "hello2.txt");
        File meta2 = new File(new File(rootPath, "mydirectory"), "hello2.txt.meta.json");

        // Both files and meta json should exist on the file system
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file1.isFile());
        Assert.assertTrue(meta1.exists());
        Assert.assertTrue(meta1.isFile());
        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file2.isFile());
        Assert.assertTrue(meta2.exists());
        Assert.assertTrue(meta2.isFile());

        // Test existsPath method
        Assert.assertTrue(storage.existsPath("/mydirectory"));
        Assert.assertTrue(storage.existsPath("mydirectory"));
    }

    @Test
    public void testGetDataObjectCount() throws StorageException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);

        Assert.assertEquals(3, storage.getDataObjectCount());
    }

    @Test
    public void testGetDataObjects() throws StorageException, IOException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);

        List<DataObject> dataobjects = new ArrayList<>();
        Iterator<DataObject> it = storage.getDataObjects();
        while (it.hasNext())
            dataobjects.add(it.next());

        // Storage should return two data objects
        Assert.assertEquals(2, dataobjects.size());

        // Data Objects should be equal to test texts
        List<String> expected = new ArrayList<>();
        expected.add(TEST_TXT_1);
        expected.add(TEST_TXT_2);

        String actual1 = new String(dataobjects.get(0).getBytes());
        String actual2 = new String(dataobjects.get(1).getBytes());

        Assert.assertTrue(expected.contains(actual1));
        Assert.assertTrue(expected.contains(actual2));
        Assert.assertFalse(actual1.equals(actual2));
    }

    @Test
    public void testMoveFile() throws StorageException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);

        // Prepare the test: Add two test files
        storage.addFile(new StringInputStream(TEST_TXT_1), "/mydirectory/hello1.txt", new MetainfoContainer());
        storage.addFile(new StringInputStream(TEST_TXT_2), "/mydirectory/hello2.txt", new MetainfoContainer());

        // Move directory
        storage.move("/mydirectory", "/my-new-directory");

        File dir1 = new File(ROOT_PATH, "my-new-directory");
        File file1 = new File(dir1, "hello1.txt");
        File file2 = new File(dir1, "hello2.txt");
        File meta1 = new File(dir1, "hello1.txt.meta.json");
        File meta2 = new File(dir1, "hello2.txt.meta.json");
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file1.isFile());
        Assert.assertTrue(meta1.exists());
        Assert.assertTrue(meta1.isFile());

        Assert.assertTrue(file2.exists());
        Assert.assertTrue(file2.isFile());
        Assert.assertTrue(meta2.exists());
        Assert.assertTrue(meta2.isFile());

        // Move file
        storage.move("/my-new-directory/hello1.txt", "/yet-another-directory/hello3.txt");
        File dir2 = new File(ROOT_PATH, "yet-another-directory");
        File file3 = new File(dir2, "hello3.txt");
        File meta3 = new File(dir2, "hello3.txt.meta.json");
        Assert.assertTrue(file3.exists());
        Assert.assertTrue(file3.isFile());
        Assert.assertTrue(meta3.exists());
        Assert.assertTrue(meta3.isFile());
    }

    @Ignore
    @Test
    public void testRemoveFile() throws StorageException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);

        File file = new File(new File(ROOT_PATH, "my-new-directory"), "hello3.txt");
        try {
            file.createNewFile();
        } catch (IOException e1) {
        }

        File meta = new File(new File(ROOT_PATH, "my-new-directory"), "hello3.txt.meta.json");
        try {
            meta.createNewFile();
        } catch (IOException e) {
        }

        Assert.assertTrue(file.exists());
        Assert.assertTrue(meta.exists());

        storage.removeFile("/my-new-directory/hello3.txt");

        Assert.assertFalse(file.exists());
        Assert.assertFalse(meta.exists());
    }

    @Test
    public void testDelete() throws StorageException {
        Storage storage = new LocalFilesystemStorage();
        storage.open(ROOT_PATH);
        storage.delete();

        File rootPath = new File(ROOT_PATH);
        Assert.assertFalse(rootPath.exists());
    }
}
