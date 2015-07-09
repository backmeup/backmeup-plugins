/**
 * @author richard
 */

package facebook.storage;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.types.Album;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import com.restfb.types.User;

import facebook.utils.ConsoleDrawer;

public class Serializer
{
	public Serializer(String workingdir, long maxPics, ArrayList<String> skipAlbums)
	{
		this.path = workingdir;
		this.maxPics = maxPics;
		this.skipAlbums = skipAlbums;
	}

	private String path;
	private long maxPics;
	private ArrayList<String> skipAlbums;

	public HashMap<Object, Object> userInfo(User user, FacebookClient fcb, boolean thisIsMe, boolean makeDirs)
	{
		if (user == null)
			return new HashMap<Object, Object>();
		HashMap<Object, Object> infos = new HashMap<Object, Object>();
		infos.put(UserInfoKeys.ABOUT, user.getAbout());
		infos.put(UserInfoKeys.DATE_OF_BIRTH, user.getBirthdayAsDate().getTime());
		infos.put(UserInfoKeys.FIRST_NAME, user.getFirstName());
		infos.put(UserInfoKeys.LAST_NAME, user.getLastName());
		infos.put(UserInfoKeys.GENDER, user.getGender());
		infos.put(UserInfoKeys.METADATA, user.getMetadata());
		infos.put(UserInfoKeys.PROFILE_PICTURE, user.getPicture());
		infos.put(UserInfoKeys.WEBSITE, user.getWebsite());
		infos.put(UserInfoKeys.ORIGINAL_LINK, user.getLink());
		infos.put(UserInfoKeys.LOCALE, user.getLocale());
		infos.put(UserInfoKeys.LANGUAGES, user.getLanguages());
		infos.put(UserInfoKeys.HOME_TOWN, user.getHometownName());
		infos.put(UserInfoKeys.ID, user.getId());
		if (thisIsMe && fcb != null)
		{
			Connection<Album> albums = fcb.fetchConnection("me/albums", Album.class);
			for (Album album : albums.getData())
			{
				albumInfo(album, fcb);
			}
		}
		Properties props = new Properties();
		HashMap<String, String> newinfos = dataValidatot(infos);
		props.putAll(newinfos);
		try
		{
			props.storeToXML(new FileOutputStream(new File(path + SDO.SLASH + FilePaths.USER_FILE)), "General user info");
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infos;
	}

	public HashMap<Object, Object> albumInfo(Album album, FacebookClient fcb)
	{
		HashMap<Object, Object> infos = new HashMap<>();
		if (album == null)
			return infos;
		infos.put(AlbumInfoKeys.COUNT, album.getCount());
		infos.put(AlbumInfoKeys.COVER_PHOTO_ID, album.getCoverPhoto());
		infos.put(AlbumInfoKeys.CREATED, album.getCreatedTime().getTime());
		infos.put(AlbumInfoKeys.DESCRIPTION, album.getDescription());
		infos.put(AlbumInfoKeys.LAST_UPDATE, album.getUpdatedTime().getTime());
		infos.put(AlbumInfoKeys.ORIGINAL_LINK, album.getLink());
		infos.put(AlbumInfoKeys.PRIVACY, album.getPrivacy());
		infos.put(AlbumInfoKeys.NAME, album.getName());
		File dir = new File(path + SDO.SLASH + FilePaths.ALBUM_DIRECTORY.toString().replace("" + ReplaceID.ALBUM_ID, album.getId()));

		if (!skipAlbums.contains(album.getName()))
		{
			Connection<Photo> photosConn = fcb.fetchConnection(album.getId() + "/photos", Photo.class);
			System.out.print("Fetching Photos from " + album.getName() + ". This may take a while...\t\t");
			long iterator = 0;
			Iterator<List<Photo>> it = photosConn.iterator();
			boolean breakLoop = false;
			while (it.hasNext())
			{
				List<Photo> photos = it.next();
				for (Photo photo : photos)
				{
					if (iterator >= maxPics && maxPics >= 0)
					{
						breakLoop = true;
						break;
					}
					ConsoleDrawer.drawProgress(20, (int) ((iterator / (((maxPics < 0) ? album.getCount() : maxPics) * 1.0)) * 20), iterator == 0);
					if (!dir.exists())
						dir.mkdirs();
					photoInfo(photo, dir);
					iterator++;
				}
				if (breakLoop)
					break;
			}
			ConsoleDrawer.drawProgress(20, 20, false);
			System.out.println();
			Properties pros = new Properties();
			HashMap<String, String> newinfos = dataValidatot(infos);
			pros.putAll(newinfos);
			try
			{
				pros.storeToXML(new FileOutputStream(new File(dir + "/albuminfo.xml")), "Info about album " + album.getId());
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			System.out.println("Skipped album " + album.getName());
		return infos;
	}

	public static HashMap<Object, Object> photoInfo(Photo photo, File directory)
	{
		HashMap<Object, Object> infos = new HashMap<>();
		if (photo == null)
			return infos;
		infos.put(PhotoInfoKeys.FILE, photo.getId() + ".jpg");
		infos.put(PhotoInfoKeys.BACK_DATE, (photo.getBackdatedTime() == null) ? null : photo.getBackdatedTime().getTime());
		infos.put(PhotoInfoKeys.COMMENT_DIR, photo.getId() + "_COMMENTS");
		infos.put(PhotoInfoKeys.LAST_UPDATE, photo.getUpdatedTime().getTime());
		infos.put(PhotoInfoKeys.ORIGINAL_LINK, photo.getLink());
		infos.put(PhotoInfoKeys.LIKES_FROM_PEOPLE, genLikes(photo.getLikes()));
		infos.put(PhotoInfoKeys.LIKES, photo.getLikes().size());
		infos.put(PhotoInfoKeys.PLACE, photo.getPlace());
		infos.put(PhotoInfoKeys.PUBLISH_DATE, photo.getCreatedTime().getTime());
		infos.put(PhotoInfoKeys.ID, photo.getId());
		HashMap<String, String> newinfos = dataValidatot(infos);
		Properties props = new Properties();
		props.putAll(newinfos);
		try (FileOutputStream fos = new FileOutputStream(new File("" + directory + SDO.SLASH + FilePaths.PHOTO_INFO.toString().replace("" + ReplaceID.PHOTO_ID, photo.getId()))))
		{
			props.storeToXML(fos, "Infos about photo " + photo.getId());
			fos.flush();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = photo.getSource();
		String[] parts = url.split("/");
		// remove URL parts which decrease the resolution
		if (parts.length > 7)
		{
			parts[5] = null;
			parts[6] = null;

			StringBuilder sb = new StringBuilder();
			for (String s : parts)
				if (s != null)
				{
					sb.append(s);
					sb.append("/");
				}
			sb.delete(sb.length() - 1, sb.length());
			// System.out.println(url);
			url = sb.toString();
		}
		// System.out.println(url);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); FileOutputStream fw = new FileOutputStream("" + directory + SDO.SLASH + infos.get(PhotoInfoKeys.FILE)); BufferedInputStream br = new BufferedInputStream(new URL(url).openStream());)
		{
			byte[] puffer = new byte[1024];
			int i = 0;
			while ((i = br.read(puffer)) != -1)
			{
				baos.write(puffer, 0, i);
			}
			byte[] result = baos.toByteArray();
			fw.write(result);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infos;
	}

	public static Properties getReadableUserInfos(HashMap<Object, Object> userInfos)
	{
		Properties infos = new Properties();
		if (userInfos == null)
			return infos;
		for (Object uik : userInfos.keySet())
		{
			Object value = userInfos.get(uik);
			if (value != null)
			{
				/*
				 * if (value instanceof Collection<?>) { Collection<?> coll =
				 * (Collection<?>) value; for (Object o : coll) {
				 * System.out.println(o); } }
				 */
				infos.put(uik.toString(), value.toString());
			}
		}
		return infos;
	}

	public static HashMap<String, String> dataValidatot(HashMap<Object, Object> map)
	{
		HashMap<String, String> newMap = new HashMap<>();
		if (map == null)
			return newMap;
		Iterator<Object> it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			if (map.get(key) != null)
				newMap.put(key.toString(), map.get(key).toString());
		}
		return newMap;
	}

	public static String genLikes(List<NamedFacebookType> likes)
	{
		if (likes == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (NamedFacebookType nft : likes)
		{
			sb.append(nft.getName() + ";");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
