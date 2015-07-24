package facebook.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.backmeup.facebook.files.ConfLoader;
import org.backmeup.facebook.files.PropertyOption;
import org.backmeup.facebook.storage.Serializer;
import org.junit.Before;
import org.junit.Test;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.experimental.api.Facebook;

public class DownloadTester
{

	private FacebookClient fbc;
	private Facebook facebook;
	private Long maxPics;
	private ArrayList<String> skipAlbums;
	private int circles = 0;
	private File dir;

	@Before
	public void testSerializer()
	{
		if (circles == 0)
		{
			if (!ConfLoader.confExists())
				ConfLoader.genProperties();
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
		}
		circles++;
	}

	@Test
	public void testAll()
	{
		Serializer.generateAll(fbc, facebook, dir, skipAlbums, maxPics);
	}

}
