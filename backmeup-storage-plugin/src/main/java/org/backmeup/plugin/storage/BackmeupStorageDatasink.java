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
import org.backmeup.plugin.storage.Constants.Constants;
import org.backmeup.storage.client.BackmeupStorageClient;
import org.backmeup.storage.client.StorageClient;

/**
 * The BackmeupStorageDatasink class uploads all elements from the StorageReader up to
 * the Backmeup-Storage service based on the connection string of a certain user.
 * 
 */
public class BackmeupStorageDatasink implements Datasink {

	@Override
	public String upload(Properties authData, Properties properties, List<String> options, Storage storage, Progressable progressor) throws StorageException {
		StorageClient client = null;
		try {
		client = new BackmeupStorageClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String tmpDir = authData.getProperty("org.backmeup.tmpdir");
		if (tmpDir == null) {
		  tmpDir = "";
		}
		
		String connectionString = authData.getProperty(Constants.PROP_CONNECTION_STRING);
		String accessToken = connectionString;
		
		Iterator<DataObject> it = storage.getDataObjects();		
		int i = 1;
		while(it.hasNext()) {
			DataObject dataObj = it.next();
			
			String filepath = "/" + tmpDir + dataObj.getPath();
			
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
