package org.backmeup.facebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.facebook.files.ConfLoader;
import org.backmeup.facebook.files.PropertyOption;
import org.backmeup.facebook.htmlgenerator.HTMLGenerator;
import org.backmeup.facebook.storage.Serializer;
import org.backmeup.facebook.utils.FileUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.experimental.api.Facebook;

public class FacebookDatasource implements Datasource
{

	public static void main(String[] args)
	{
		FacebookClient fbc;
		Facebook facebook;
		Long maxPics;
		ArrayList<String> skipAlbums;
		File dir;
		HTMLGenerator mainGen;
		String path;
		File target;
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
		if (!ConfLoader.confExists())
			ConfLoader.genProperties();
		Properties props = ConfLoader.getProperties();
		path = props.getProperty(PropertyOption.DIRECTORY.toString());
		if (!ConfLoader.confExists())
			ConfLoader.genProperties();
		if (arguments.contains("--download"))
		{
			String CURRENT_ACCESSTOKEN = ConfLoader.getProperties().getProperty(PropertyOption.ACCESS_TOKEN.toString());
			maxPics = (long) -1;
			try
			{
				maxPics = Long.parseLong(ConfLoader.getProperties().getProperty(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString()));
			} catch (NumberFormatException e)
			{

			}
			skipAlbums = new ArrayList<>();
			skipAlbums.addAll(Arrays.asList(ConfLoader.getProperties().getProperty(PropertyOption.SKIP_ALBUMS.toString()).split(";")));
			fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
			facebook = new Facebook(fbc);
			dir = new File(ConfLoader.getProperties().getProperty(PropertyOption.DIRECTORY.toString()));
			Serializer.generateAll(fbc, facebook, dir, skipAlbums, maxPics);
		}
		if (arguments.contains("--generate-html"))
		{
			mainGen = new HTMLGenerator();
			target = new File(props.getProperty(PropertyOption.HTML_DIR.toString()));
			if (!target.exists())
				target.mkdirs();
			FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/main.css", new File("" + target + "/main.css"), HTMLGenerator.class);
			FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/menu.css", new File("" + target + "/menu.css"), HTMLGenerator.class);
			mainGen.genOverview(target, new File(path));
		}
	}

	@Override
	public void downloadAll(Map<String, String> authData, Map<String, String> properties, List<String> options, Storage storage, Progressable progressor) throws DatasourceException, StorageException
	{
		FacebookClient fbc;
		Facebook facebook;
		Long maxPics;
		ArrayList<String> skipAlbums;
		File dir;
		HTMLGenerator mainGen;
		File target;

		if (!ConfLoader.confExists() && properties != null)
			ConfLoader.genProperties();
		Properties props;
		if (properties == null)
			props = ConfLoader.getProperties();
		else
		{
			props = new Properties();
			props.putAll(properties);
		}
		if (!ConfLoader.confExists())
			ConfLoader.genProperties();
		/*
		 * if (options.contains("--download")) {
		 */
		String CURRENT_ACCESSTOKEN = props.getProperty(PropertyOption.ACCESS_TOKEN.toString());
		maxPics = (long) -1;
		try
		{
			maxPics = Long.parseLong(props.getProperty(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString()));
		} catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		skipAlbums = new ArrayList<>();
		// skipAlbums.addAll(Arrays.asList(ConfLoader.getProperties().getProperty(PropertyOption.SKIP_ALBUMS.toString()).split(";")));
		fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
		facebook = new Facebook(fbc);
		String tDir = System.getProperty("java.io.tmpdir");
		dir = new File(tDir + "/xmldata/.core.xml");
		Serializer.generateAll(fbc, facebook, dir, skipAlbums, maxPics);
		/*
		 * } if (options.contains("--generate-html")) {
		 */
		mainGen = new HTMLGenerator();
		target = new File(tDir + "/html");
		try (FileInputStream fishtml = new FileInputStream(target); FileInputStream fisxml = new FileInputStream(dir.getParentFile()))
		{
			storage.addFile(fisxml, "/xmldata", new MetainfoContainer());
			storage.addFile(fishtml, "/html", new MetainfoContainer());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if (!target.exists())
			target.mkdirs();
		FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/main.css", new File("" + target + "/main.css"), HTMLGenerator.class);
		FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/menu.css", new File("" + target + "/menu.css"), HTMLGenerator.class);
		mainGen.genOverview(target, dir);
		// }
	}

}
