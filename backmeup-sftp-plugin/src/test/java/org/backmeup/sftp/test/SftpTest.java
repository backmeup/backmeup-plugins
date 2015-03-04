package org.backmeup.sftp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.sftp.SftpDatasource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SftpTest {
	@Rule
    public SftpServerSetup sftpServer = new SftpServerSetup();

	@Before
    public void before() {
        //sftpServer.start();
    }

	@After
    public void after() {
		//sftpServer.stop();
    }
    
    @Test
	public void testDownloadAll() {
		// Use the properties saved during DropboxAuthenticate to download all
		// files from Dropbox
		Map<String, String> authProps = new HashMap<String, String>();
		
		Properties tmp = new Properties();
		try {
			tmp.load(new FileReader(new File("auth.props")));
		} catch (Exception  e) {
			Assert.fail("Test failed : " + e.getMessage());
		}
		
		for (Entry<Object, Object> entry : tmp.entrySet()) {
			authProps.put((String)entry.getKey(), (String)entry.getValue());
		}

		Map<String, String> props = new HashMap<String, String>();
		List<String> options = new ArrayList<>();

		SftpDatasource source = new SftpDatasource();
		Storage storage = new LocalFilesystemStorage();
		try {
			storage.open("C:/TEMP/TEST/");
			source.downloadAll(authProps, props, options, storage,
					new Progressable() {
						@Override
						public void progress(String message) {
							System.out.println(message);
						}
					});
		
			// StorageReader sr = new LocalFilesystemStorageReader();
			// sr.open("C:/TEMP/TEST/");
			Iterator<DataObject> it = storage.getDataObjects();
			while (it.hasNext()) {
				DataObject da = it.next();
				System.out.println(da.getMetainfo());
				System.out.println();
			}
		} catch (Exception e) {
			Assert.fail("Test failed : " + e.getMessage());
		}
	}
}
