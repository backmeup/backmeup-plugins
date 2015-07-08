package facebook.main;

import static org.junit.Assert.fail;

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

	private Serializer ser;
	private FacebookClient fbc;
	private Facebook facebook;
	private int circles = 0;

	@Before
	public void testSerializer()
	{
		if (circles == 0)
		{
			ser = new Serializer(ConfLoader.getProperties().getProperty(PropertyOption.DIRECTORY.toString()));
			String CURRENT_ACCESSTOKEN = ConfLoader.getProperties().getProperty(PropertyOption.ACCESS_TOKEN.toString());
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
		if (Serializer.dataValidatot(null) == null)
			fail("Not allowed to be null");
	}

	@Test
	public void testGenLikes()
	{
		if (Serializer.genLikes(null) == null)
			fail("Not allowed to be null");
	}

}
