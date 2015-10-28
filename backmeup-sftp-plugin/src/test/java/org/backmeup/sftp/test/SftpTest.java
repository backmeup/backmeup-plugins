package org.backmeup.sftp.test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.sftp.SftpDatasource;
import org.backmeup.sftp.SftpDescriptor;
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
        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(SftpDescriptor.SFTP_ID);
        pluginProfile.setProfileType(PluginType.Source);
        pluginProfile.setAuthData(new AuthDataDTO());
        pluginProfile.setProperties(new HashMap<String, String>());
        pluginProfile.setOptions(new ArrayList<String>());
		
		Properties tmp = new Properties();
		try {
			tmp.load(new FileReader(new File("auth.props")));
		} catch (Exception  e) {
			Assert.fail("Test failed : " + e.getMessage());
		}
		
		for (Entry<Object, Object> entry : tmp.entrySet()) {
			pluginProfile.getAuthData().addProperty((String)entry.getKey(), (String)entry.getValue());
		}

		SftpDatasource source = new SftpDatasource();
		Storage storage = new LocalFilesystemStorage();
		try {
			storage.open("C:/TEMP/TEST/");
			source.downloadAll(pluginProfile, new PluginContext(), storage,
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
