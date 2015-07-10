package facebook.main;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.experimental.api.Facebook;

import facebook.files.ConfLoader;
import facebook.files.PropertyOption;
import facebook.storage.Serializer;

public class DownloadTester
{

	private static final boolean genConf = false;
	private Serializer ser;
	private FacebookClient fbc;
	private Facebook facebook;
	private Long maxPics;
	private ArrayList<String> skipAlbums;
	private int circles = 0;

	@Before
	public void testSerializer()
	{
		if (circles == 0)
		{
			if (genConf)
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
			ser = new Serializer(ConfLoader.getProperties().getProperty(PropertyOption.DIRECTORY.toString()), maxPics, skipAlbums);
			fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
			facebook = new Facebook(fbc);
		}
		circles++;
	}

	@Test
	public void testUserInfo()
	{
		ser.userInfo(facebook.users().getMe(), fbc, true, true);
	}

	@Test
	public void testAlbumInfo()
	{
		ser.albumInfo(null, fbc);
	}

	@Test
	public void testPhotoInfo()
	{
		if (Serializer.photoInfo(null, null) == null)
			fail("Not allowed to be null");
	}

	@Test
	public void testGetReadableUserInfos()
	{
		if (Serializer.getReadableUserInfos(null) == null)
			fail("Not allowed to be null");
	}

	@Test
	public void testDataValidatot()
	{
		if (Serializer.dataValidator(null) == null)
			fail("Not allowed to be null");
	}

	@Test
	public void testGenLikes()
	{
		if (Serializer.genLikes(null) == null)
			fail("Not allowed to be null");
	}

}
