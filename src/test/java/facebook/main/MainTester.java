package facebook.main;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.experimental.api.Facebook;
import com.restfb.types.Album;

import facebook.storage.FilePaths;
import facebook.storage.ReplaceID;
import facebook.storage.SDO;
import facebook.storage.Serializer;

public class MainTester
{
	private static int circles = 0;
	public final static String API_KEY = "", API_SECRET = "";
	public static String CURRENT_ACCESSTOKEN = "";
	FacebookClient fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
	Facebook facebook = new Facebook(fbc);

	@Before
	public void forceJUnit()
	{
		JUnitCore.runClasses(FirstTest.class);
		System.out.println("sSkM");
	}
	
	@Test
	public void testUserInfo()
	{
		try
		{
			Serializer.userInfo(null, null, true, true);
		} catch (NullPointerException e)
		{
			fail("NullPointerException on: " + e.getStackTrace()[0].toString());
		}
		Serializer.userInfo(facebook.users().getMe(), fbc, true, true);
		if (!new File(Serializer.PATH).exists())
			fail(Serializer.PATH + " does not exist");
	}

	@Before
	public void testExpiredSession()
	{
		try
		{
			fbc.fetchConnection("me/albums", Album.class);
		} catch (FacebookOAuthException fe)
		{
			if (fe.getMessage().contains("expired"))
				fail("Please generate a new AccessToken");
			else
				fail("Other reason: " + fe.getMessage());
		}
	}

	@Test
	public void testAlbumInfo()
	{
		try
		{
			Serializer.albumInfo(null, null);
		} catch (NullPointerException e)
		{
			fail("NullPointerException on: " + e.getStackTrace()[0].toString());
		}

	}

	@Test(expected = FacebookOAuthException.class)
	public void testInvalidAlbumNames()
	{
		for (int i = 0; i <= 30; i++)
		{
			Album a = new Album();
			String id = "Test-" + Math.random();
			a.setId(id);
			Serializer.albumInfo(a, fbc);
			if (new File("" + Serializer.PATH + SDO.SLASH + FilePaths.ALBUM_DIRECTORY.toString().replace("" + ReplaceID.ALBUM_ID, id)).exists())
				fail("Album with id " + id + "does not exist");
		}
	}

	@After
	public void checkEmptyDirs()
	{
		File root = new File(Serializer.PATH);
		if (circles == 2)
		{
			String list = emptyDirs(root);
			if (list.length() > 0)
				System.out.println("Warning: Empty Directories: \n" + list);
		}
		circles++;
	}

	public String emptyDirs(File file)
	{
		StringBuilder sb = new StringBuilder();
		if (file.exists() && file.isDirectory())
		{
			if (file.listFiles().length == 0 && !file.toString().equals(Serializer.PATH))
				sb.append(file.toString() + "\n");
			else
				for (File f : file.listFiles())
					sb.append(emptyDirs(f));
		}
		return sb.toString();
	}

	@Test
	public void testPhotoInfo()
	{
		try
		{
			Serializer.photoInfo(null, null);
		} catch (NullPointerException e)
		{
			fail("NullPointerException on: " + e.getStackTrace()[0].toString());
		}

	}

	@Test
	public void testGetReadableUserInfos()
	{
		try
		{
			Serializer.getReadableUserInfos(null);
		} catch (NullPointerException e)
		{
			fail("NullPointerException on: " + e.getStackTrace()[0].toString());
		}
	}

	@Test
	public void testDataValidatot()
	{
		try
		{
			Serializer.dataValidatot(null);
		} catch (NullPointerException e)
		{
			fail("NullPointerException on: " + e.getStackTrace()[0].toString());
		}
	}

	@Test
	public void testGenLikes()
	{
		try
		{
			Serializer.genLikes(null);
		} catch (NullPointerException e)
		{
			fail("NullPointerException on: " + e.getStackTrace()[0].toString());
		}
	}
}
