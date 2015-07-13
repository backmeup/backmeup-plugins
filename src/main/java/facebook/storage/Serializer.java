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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.hp.gagawa.java.elements.A;
import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.experimental.api.Facebook;
import com.restfb.types.Album;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.Group;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import com.restfb.types.Photo.Tag;
import com.restfb.types.Place;
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

	public HashMap<SerializerKey, Object> userInfo(User user, FacebookClient fcb, Facebook facebook, boolean thisIsMe, boolean makeDirs)
	{
		if (user == null)
			return new HashMap<SerializerKey, Object>();
		HashMap<SerializerKey, Object> infos = new HashMap<SerializerKey, Object>();
		infos.put(UserInfoKey.ABOUT, user.getAbout());
		infos.put(UserInfoKey.DATE_OF_BIRTH, user.getBirthdayAsDate());
		infos.put(UserInfoKey.FIRST_NAME, user.getFirstName());
		infos.put(UserInfoKey.LAST_NAME, user.getLastName());
		infos.put(UserInfoKey.GENDER, user.getGender());
		infos.put(UserInfoKey.METADATA, user.getMetadata());
		infos.put(UserInfoKey.PROFILE_PICTURE, user.getPicture());
		infos.put(UserInfoKey.WEBSITE, user.getWebsite());
		infos.put(UserInfoKey.ORIGINAL_LINK, user.getLink());
		infos.put(UserInfoKey.LOCALE, user.getLocale());
		infos.put(UserInfoKey.LANGUAGES, user.getLanguages());
		infos.put(UserInfoKey.HOME_TOWN, user.getHometownName());
		infos.put(UserInfoKey.ID, user.getId());
		infos.put(UserInfoKey.BIO, user.getBio());
		infos.put(UserInfoKey.CURRENCY, user.getCurrency());
		infos.put(UserInfoKey.EDUCATION, user.getEducation());
		infos.put(UserInfoKey.EMAIL, user.getEmail());
		infos.put(UserInfoKey.FAVOURITE_ATHLETS, user.getFavoriteAthletes());
		infos.put(UserInfoKey.FAVOURITE_TEAMS, user.getFavoriteTeams());
		infos.put(UserInfoKey.INTERESTED_IN, user.getInterestedIn());
		infos.put(UserInfoKey.LAST_UPDATED, user.getUpdatedTime());
		infos.put(UserInfoKey.MEETING_FOR, user.getMeetingFor());
		infos.put(UserInfoKey.POLITICAL, user.getPolitical());
		infos.put(UserInfoKey.QUOTES, user.getQuotes());
		infos.put(UserInfoKey.RELATIONSHIP_STATUS, user.getRelationshipStatus());
		infos.put(UserInfoKey.RELIGION, user.getReligion());
		infos.put(UserInfoKey.SIGNIFICANT_OTHER, user.getSignificantOther());
		infos.put(UserInfoKey.SPORTS, user.getSports());
		infos.put(UserInfoKey.THIRD_PARTY_ID, user.getThirdPartyId());
		infos.put(UserInfoKey.TIMEZONE, user.getTimezone());
		infos.put(UserInfoKey.TOKEN_FOR_BUISSNESS, user.getTokenForBusiness());
		infos.put(UserInfoKey.VERIFIED, user.getVerified());
		infos.put(UserInfoKey.WORK, user.getWork());
		infos.put(UserInfoKey.NAME, user.getName());
		infos.put(UserInfoKey.MIDDLE_NAME, user.getMiddleName());
		if (thisIsMe && fcb != null)
		{
			Connection<Album> albums = fcb.fetchConnection("me/albums", Album.class);
			if (!albums.getData().isEmpty())
				for (Album album : albums.getData())
				{
					albumInfo(album, fcb);
				}
		}
		Connection<Group> groups = fcb.fetchConnection("me/groups", Group.class);
		for (Group group : groups.getData())
			groupInfo(group, fcb);
		Properties props = new Properties();
		HashMap<String, String> newinfos = dataValidator(infos);
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

	public HashMap<SerializerKey, Object> groupInfo(Group group, FacebookClient fbc)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		if (group == null)
			return infos;
		infos.put(GroupInfoKey.DESCRIPTION, group.getDescription());
		infos.put(GroupInfoKey.ICON, group.getIcon());
		infos.put(GroupInfoKey.ID, group.getId());
		infos.put(GroupInfoKey.LAST_UPDATE, group.getUpdatedTime());
		infos.put(GroupInfoKey.LINK, group.getLink());
		infos.put(GroupInfoKey.METATDATA, group.getMetadata());
		infos.put(GroupInfoKey.NAME, group.getName());
		infos.put(GroupInfoKey.OWNER, group.getOwner());
		infos.put(GroupInfoKey.PRIVACY, group.getPrivacy());
		infos.put(GroupInfoKey.VENUE, group.getVenue());
		File dir = new File(path + SDO.SLASH + "groups");
		if (!dir.exists())
			dir.mkdirs();
		File file = new File("" + dir + SDO.SLASH + group.getId() + ".xml");
		Properties props = new Properties();
		props.putAll(dataValidator(infos));
		try (FileOutputStream fos = new FileOutputStream(file))
		{
			props.storeToXML(fos, "Represents a group");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return infos;
	}

	public HashMap<SerializerKey, Object> albumInfo(Album album, FacebookClient fcb)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		if (album == null)
			return infos;
		infos.put(AlbumInfoKey.COUNT, album.getCount());
		infos.put(AlbumInfoKey.COVER_PHOTO_ID, album.getCoverPhoto());
		infos.put(AlbumInfoKey.CREATED, album.getCreatedTime());
		infos.put(AlbumInfoKey.DESCRIPTION, album.getDescription());
		infos.put(AlbumInfoKey.LAST_UPDATE, album.getUpdatedTime());
		infos.put(AlbumInfoKey.ORIGINAL_LINK, album.getLink());
		infos.put(AlbumInfoKey.PRIVACY, album.getPrivacy());
		infos.put(AlbumInfoKey.NAME, album.getName());
		infos.put(AlbumInfoKey.COMES_FROM, album.getFrom());
		infos.put(AlbumInfoKey.LOCATION, album.getLocation());
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
			infos.put(AlbumInfoKey.LOCAL_COUNT, iterator);
			System.out.println();
			Properties pros = new Properties();
			HashMap<String, String> newinfos = dataValidator(infos);
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

	public static HashMap<SerializerKey, Object> photoInfo(Photo photo, File directory)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		if (photo == null)
			return infos;
		infos.put(PhotoInfoKey.FILE, photo.getId() + ".jpg");
		infos.put(PhotoInfoKey.BACK_DATE, (photo.getBackdatedTime() == null) ? null : photo.getBackdatedTime());
		infos.put(PhotoInfoKey.COMMENT_DIR, photo.getId() + "_COMMENTS");
		infos.put(PhotoInfoKey.LAST_UPDATE, photo.getUpdatedTime());
		infos.put(PhotoInfoKey.ORIGINAL_LINK, photo.getLink());
		infos.put(PhotoInfoKey.LIKES_FROM_PEOPLE, photo.getLikes());
		infos.put(PhotoInfoKey.LIKES, photo.getLikes().size());
		infos.put(PhotoInfoKey.PLACE, photo.getPlace());
		infos.put(PhotoInfoKey.PUBLISH_DATE, photo.getCreatedTime());
		infos.put(PhotoInfoKey.ID, photo.getId());
		infos.put(PhotoInfoKey.FROM, photo.getFrom());
		infos.put(PhotoInfoKey.TAGS, photo.getTags());
		HashMap<String, String> newinfos = dataValidator(infos);
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
		for (Comment comment : photo.getComments())
		{
			commentInfo(comment, new File("" + directory + SDO.SLASH + infos.get(PhotoInfoKey.COMMENT_DIR) + SDO.SLASH + comment.getId()));
		}
		downloadPhoto(photo, new File("" + directory + SDO.SLASH + infos.get(PhotoInfoKey.FILE)));
		return infos;
	}

	public static void downloadPhoto(Photo photo, File out)
	{
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
			url = sb.toString();
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); FileOutputStream fw = new FileOutputStream(out); BufferedInputStream br = new BufferedInputStream(new URL(url).openStream());)
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
	}

	public static void commentInfo(Comment comment, File dir)
	{
		File commentInfo = new File("" + dir + SDO.SLASH + "commentinfo.xml");
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		infos.put(CommentKey.ATTACHMENT, comment.getAttachment());
		infos.put(CommentKey.CAN_REMOVE, comment.getCanRemove());
		infos.put(CommentKey.CREATED, comment.getCreatedTime());
		infos.put(CommentKey.FROM, comment.getFrom());
		infos.put(CommentKey.HIDDEN, comment.getIsHidden());
		infos.put(CommentKey.ID, comment.getId());
		infos.put(CommentKey.LIKE_COUNT, comment.getLikeCount());
		infos.put(CommentKey.MESSAGE, comment.getMessage());
		infos.put(CommentKey.REPLIES_COUNT, comment.getCommentCount());
		infos.put(CommentKey.METADATA, comment.getMetadata());
		HashMap<String, String> newInfos = dataValidator(infos);
		Properties props = new Properties();
		props.putAll(newInfos);
		if (!dir.exists())
			dir.mkdirs();
		try (FileOutputStream fos = new FileOutputStream(commentInfo))
		{
			props.storeToXML(fos, "Represents a comment");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if (comment.getComments() != null)
			for (Comment c : comment.getComments().getData())
				commentInfo(c, new File("" + dir + SDO.SLASH + c.getId()));
	}

	public static Properties getReadableUserInfos(HashMap<SerializerKey, Object> userInfos)
	{
		Properties infos = new Properties();
		if (userInfos == null)
			return infos;
		for (Object uik : userInfos.keySet())
		{
			Object value = userInfos.get(uik);
			if (value != null)
				infos.put(uik.toString(), value.toString());
		}
		return infos;
	}

	public static HashMap<String, String> dataValidator(HashMap<SerializerKey, Object> map)
	{
		HashMap<String, String> newMap = new HashMap<>();
		if (map == null)
			return newMap;
		Iterator<SerializerKey> it = map.keySet().iterator();
		while (it.hasNext())
		{
			SerializerKey key = it.next();
			if (key != null && map.get(key) != null && key.getType() != null)
			{
				Object value = map.get(key);
				String stringKey = key.toString();
				String stringValue = null;
				switch (key.getType())
				{
				case OTHER:
				{
					stringValue = packList(value);
					break;
				}
				case DATE:
				{
					if (value instanceof Date)
						stringValue = "" + ((Date) value).getTime();
					break;
				}
				case LIST:
				{
					if (value instanceof List<?>)
						stringValue = packList((List<?>) value);
					break;
				}
				case NFT:
				{
					if (value instanceof NamedFacebookType)
						stringValue = ((NamedFacebookType) value).getName();
					break;
				}
				case CFT:
				{
					if (value instanceof CategorizedFacebookType)
						stringValue = ((CategorizedFacebookType) value).getName();
					break;
				}
				default:
				{
					stringValue = value.toString();
				}
				}
				if (stringValue != null)
					newMap.put(stringKey, stringValue);
			}
		}
		return newMap;
	}

	public static String packList(Object object)
	{
		if (object == null)
			return null;
		StringBuilder sb = new StringBuilder();
		if (object != null)
		{
			boolean check = true;
			if (object instanceof List<?> && check)
			{
				List<?> list = (List<?>) object;
				for (Object o : list)
					sb.append(packList(o));
				check = false;
			} else if (object instanceof Place && check)
			{
				Place p = (Place) object;
				A a = new A();
				a.setHref("https://www.google.at/maps/@" + p.getLocation().getLatitude() + "," + p.getLocation().getLongitude() + ",17z");
				a.appendText(p.getName() + ", " + p.getLocation().getCity() + ", " + p.getLocation().getCountry());
				sb.append(a.write());
				check = false;
			} else if (object instanceof NamedFacebookType && check)
			{
				sb.append(((NamedFacebookType) object).getName());
				check = false;
			} else if (object instanceof CategorizedFacebookType && check)
			{
				sb.append(((CategorizedFacebookType) object).getName());
				check = false;
			} else if (object instanceof Tag && check)
			{
				Tag t = (Tag) object;
				sb.append("X: " + t.getX() + "Y: " + t.getY());
				check = false;
			} else
				sb.append(object.toString());
			sb.append(";");
		}
		if (sb.length() > 0)
			sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
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
