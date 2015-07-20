package facebook.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.experimental.api.Facebook;

import facebook.files.ConfLoader;
import facebook.files.PropertyOption;
import facebook.htmlgenerator.HTMLGenerator;
import facebook.storage.Serializer;
import facebook.utils.FileUtils;

public class Main
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
			FileUtils.exctractFromJar("/facebook/htmlgenerator/css/main.css", new File("" + target + "/main.css"), HTMLGenerator.class);
			FileUtils.exctractFromJar("/facebook/htmlgenerator/css/menu.css", new File("" + target + "/menu.css"), HTMLGenerator.class);
			mainGen.genOverview(target, new File(path));
		}
	}

}
