package org.backmeup.dropbox.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.dropbox.DropboxDatasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;

public class DropboxDatasourceTest {
    public static void main(String[] args) throws IOException, StorageException {
        // Use the properties saved during DropboxAuthenticate to download all files from Dropbox
        Properties dbxProperties = new Properties();
        dbxProperties.load(new FileInputStream(new File("auth.props")));    

        Map<String, String> authData = new HashMap<>();
        for (final String name: dbxProperties.stringPropertyNames()) {
            authData.put(name, dbxProperties.getProperty(name));
        }

        DropboxDatasource source = new DropboxDatasource();
        Storage storage = new LocalFilesystemStorage();
        storage.open("C:/TEMP/TEST/");
        source.downloadAll(authData, new HashMap<String, String>(), new ArrayList<String>(), storage, new Progressable() {
            @Override
            public void progress(String message) {}
        });

        Iterator<DataObject> it = storage.getDataObjects();
        while (it.hasNext()) {
            DataObject da = it.next();
            System.out.println(da.getMetainfo());
            System.out.println();
        }
    }
}
