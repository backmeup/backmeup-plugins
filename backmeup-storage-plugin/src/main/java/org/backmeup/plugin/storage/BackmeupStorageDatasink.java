package org.backmeup.plugin.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.storage.constants.Constants;
import org.backmeup.storage.api.StorageClient;
import org.backmeup.storage.client.BackmeupStorageClient;

/**
 * The BackmeupStorageDatasink class uploads all elements from the StorageReader up to
 * the Backmeup-Storage service based on the connection string of a certain user.
 * 
 */
public class BackmeupStorageDatasink implements Datasink {

    @Override
    public String upload(Properties authData, Properties properties, List<String> options, Storage storage, Progressable progressor) throws StorageException {
        progressor.progress("Start backmeup-storage-plugin");

        String tmpDir = authData.getProperty("org.backmeup.tmpdir");
        if (tmpDir == null) {
            tmpDir = "";
        }

        progressor.progress("Get connection string");
        progressor.progress("AuthData:");
        for (String key : authData.stringPropertyNames()) {
            String value = authData.getProperty(key);
            progressor.progress(key + ": " + value);
        }

        String storageUrl = authData.getProperty(Constants.PROP_STORAGE_URL);
        progressor.progress("Storage url= " + storageUrl);

        String accessToken = authData.getProperty(Constants.ACCESS_TOKEN);
        progressor.progress("Using Token=" + accessToken);

        StorageClient client = new BackmeupStorageClient(storageUrl);

        progressor.progress("Initialized storage client");

        Iterator<DataObject> it = storage.getDataObjects();
        int i = 1;
        progressor.progress("Start uploading ...");
        while(it.hasNext()) {
            DataObject dataObj = it.next();

            String filepath = "/" + tmpDir + dataObj.getPath();
            progressor.progress("Filepath: " + filepath);

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(dataObj.getBytes());
                String log = String.format("Uploading file %s (Number: %d)...", filepath, i++);
                progressor.progress(log);
                client.saveFile(accessToken, filepath, true, bis.available(), bis);
                bis.close();
            } catch (IOException e) {
                throw new PluginException(BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID, "Error during upload of file %s", e);
            }
        }
        return null;
    }
}
