package org.backmeup.facebook;

import java.io.File;
import java.io.IOException;

import org.backmeup.facebook.FacebookHelper;
import org.backmeup.facebook.htmlgenerator.HTMLGenerator;
import org.backmeup.facebook.storage.Serializer;
import org.backmeup.plugin.api.DatasourceException;
import org.junit.Test;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

public class DownloadAndSerializeTester
{
	@Test
	public void testGenOverview() throws IOException, DatasourceException
	{
		File tempDir = File.createTempFile("facebook_", "");
		tempDir.delete();
		tempDir.mkdir();
		
		File dataDir = new File(tempDir, FacebookHelper.getProperty(FacebookHelper.PROPERTY_DATA_DIR));
        File htmlDir = new File(tempDir, FacebookHelper.getProperty(FacebookHelper.PROPERTY_HTML_DIR));
		
        //only execute test if there is an access token in properties file
		String CURRENT_ACCESSTOKEN = FacebookHelper.getProperty(FacebookHelper.RT_PROPERTY_ACCESS_TOKEN);
		if (CURRENT_ACCESSTOKEN != null) {
			FacebookClient fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
			Serializer.generateAll(fbc, dataDir, FacebookHelper.getDebugSkipAlbums(), FacebookHelper.getDebugMaxPics(), null);
			
			HTMLGenerator mainGen = new HTMLGenerator(htmlDir, dataDir);
			mainGen.genOverview();
		}

		tempDir.delete();
	}
}
