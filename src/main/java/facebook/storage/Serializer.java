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
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.experimental.api.Facebook;
import com.restfb.types.Album;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.Group;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Page;
import com.restfb.types.Photo;
import com.restfb.types.Photo.Tag;
import com.restfb.types.Place;
import com.restfb.types.Post;
import com.restfb.types.User;

import facebook.files.CustomStringBuilder;
import facebook.files.PropertyFile;
import facebook.storage.keys.AlbumInfoKey;
import facebook.storage.keys.CommentKey;
import facebook.storage.keys.GroupInfoKey;
import facebook.storage.keys.PageInfoKey;
import facebook.storage.keys.PhotoInfoKey;
import facebook.storage.keys.PostInfoKey;
import facebook.storage.keys.SerializerKey;
import facebook.storage.keys.UserInfoKey;
import facebook.utils.ConsoleDrawer;
import facebook.utils.FileUtils;

public class Serializer
{
	public static void generateAll(FacebookClient fbc, Facebook facebook, File coreFile, ArrayList<String> skipAlbums, long maxAlbumpics)
	{
		File dir = coreFile.getParentFile();
		CustomStringBuilder builder = new CustomStringBuilder("|");
		Properties listProps = new Properties();
		File userFile = new File("" + dir + SDO.SLASH + "user.xml");
		Parameter userParam = Parameter.with("fields", "about,address,age_range,bio,birthday,context,currency,devices,education,email,first_name,gender,hometown,inspirational_people,install_type,installed,interested_in,is_verified,languages,last_name,link,location,meeting_for,middle_name,name,name_format,payment_pricepoints,test_group,political,relationship_status,religion,security_settings,significant_other,sports,quotes,third_party_id,timezone,updated_time,verified,video_upload_limits,viewer_can_send_gift,website,work,cover");
		User user = fbc.fetchObject("me", User.class, userParam);
		userInfo(user, fbc, facebook, true, true, userFile);
		Connection<Album> albums = fbc.fetchConnection("me/albums", Album.class);
		for (Album album : albums.getData())
		{
			if (!skipAlbums.contains(album.getName()))
			{
				File albumXml = new File("" + dir + SDO.SLASH + "albums" + SDO.SLASH + album.getId() + SDO.SLASH + "albuminfo.xml");
				builder.append(FileUtils.getWayTo(dir, albumXml));
				albumInfo(album, fbc, albumXml, maxAlbumpics);
			}
		}
		listProps.put(PropertyFile.ALBUMS.toString(), builder.toString());
		builder.empty();
		Connection<Post> posts = fbc.fetchConnection("me/posts", Post.class);
		for (Post post : posts.getData())
		{
			File postXml = new File("" + dir + SDO.SLASH + "posts" + SDO.SLASH + post.getId() + SDO.SLASH + "postinfo.xml");
			builder.append(FileUtils.getWayTo(dir, postXml));
			postInfo(post, postXml);
		}
		listProps.put(PropertyFile.POSTS.toString(), builder.toString());
		builder.empty();
		for (String pageToken : facebook.pages().fetchAllAccessTokens().values())
		{
			FacebookClient fc = new DefaultFacebookClient(pageToken, Version.VERSION_2_3);
			Parameter parameter = Parameter.with("fields", "id,about,access_token,affiliation,app_id,artists_we_like,attire,awards,band_interests,band_members,best_page,bio,birthday,booking_agent,built,business,can_post,category,category_list,company_overview,cover,culinary_team,current_location,description,description_html,directed_by,emails,features,food_styles,founded,general_info,general_manager,genre,global_brand_page_name,has_added_app,hometown,hours,influences,is_community_page,is_permanently_closed,is_published,is_unclaimed,is_verified,link,location,mission,mpg,name,network,new_like_count,offer_eligible,parent_page,parking,payment_options,personal_info,personal_interests,pharma_safety_info,phone,plot_outline,press_contact,price_range,produced_by,products,promotion_eligible,promotion_ineligible_reason,public_transit,record_label,release_date,restaurant_services,restaurant_specialties,schedule,screenplay_by,season,starring,store_number,studio,unread_message_count,unread_notif_count,unseen_message_count,username,website,were_here_count,written_by,checkins,likes,members");
			Page page = fc.fetchObject("me", Page.class, parameter);
			pageInfo(page, new File("" + dir + SDO.SLASH + "pages" + SDO.SLASH + page.getId() + SDO.SLASH + "pageinfo.xml"));
		}
		Parameter groupParams = Parameter.with("fields", "id,cover,description,email,icon,link,member_request_count,name,owner,parent,privacy,updated_time");
		Connection<Group> groups = fbc.fetchConnection("me/groups", Group.class, groupParams);
		for (Group group : groups.getData())
		{
			File groupXml = new File("" + dir + SDO.SLASH + "groups" + SDO.SLASH + group.getId() + SDO.SLASH + "groupinfo.xml");
			builder.append(FileUtils.getWayTo(dir, groupXml));
			groupInfo(group, fbc, groupXml);
		}
		listProps.put(PropertyFile.GROUPS.toString(), builder.toString());
		listProps.put(PropertyFile.USER.toString(), FileUtils.getWayTo(coreFile, userFile));
		try (FileOutputStream fos = new FileOutputStream(coreFile))
		{
			listProps.storeToXML(fos, "list all xmls");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static HashMap<SerializerKey, Object> userInfo(User user, FacebookClient fcb, Facebook facebook, boolean thisIsMe, boolean makeDirs, File userXml)
	{
		if (user == null)
			return new HashMap<SerializerKey, Object>();
		if (!userXml.getParentFile().exists())
			userXml.getParentFile().mkdirs();
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
		Properties props = new Properties();
		HashMap<String, String> newinfos = dataValidator(infos);
		props.putAll(newinfos);
		try (FileOutputStream fos = new FileOutputStream(userXml))
		{
			props.storeToXML(fos, "General user info");
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infos;
	}

	public static HashMap<SerializerKey, Object> postInfo(Post post, File postXml)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		if (post == null || postXml == null)
			return infos;
		infos.put(PostInfoKey.ACTION, post.getActions());
		infos.put(PostInfoKey.ADMIN, post.getAdminCreator());
		infos.put(PostInfoKey.APPLICATION, post.getApplication());
		infos.put(PostInfoKey.ATTRIBUTION, post.getAttribution());
		infos.put(PostInfoKey.CAPTION, post.getCaption());
		infos.put(PostInfoKey.COMMENTS, post.getComments());
		infos.put(PostInfoKey.COMMENTS_COUNT, post.getCommentsCount());
		infos.put(PostInfoKey.CREATED_TIME, post.getCreatedTime());
		infos.put(PostInfoKey.DESCRIPTION, post.getDescription());
		infos.put(PostInfoKey.FROM, post.getFrom());
		infos.put(PostInfoKey.ICON, post.getIcon());
		infos.put(PostInfoKey.ID, post.getId());
		infos.put(PostInfoKey.LAST_UPDATE, post.getUpdatedTime());
		infos.put(PostInfoKey.LIKES, post.getLikes());
		infos.put(PostInfoKey.LIKES_COUNT, post.getLikesCount());
		infos.put(PostInfoKey.LINK, post.getLink());
		infos.put(PostInfoKey.MESSAGE, post.getMessage());
		infos.put(PostInfoKey.MESSAGE_TAGS, post.getMessageTags());
		infos.put(PostInfoKey.OBJECT_ID, post.getObjectId());
		infos.put(PostInfoKey.PICTURE, post.getPicture());
		infos.put(PostInfoKey.PLACE, post.getPlace());
		infos.put(PostInfoKey.PRIVACY, post.getPrivacy());
		infos.put(PostInfoKey.PROPERTIES, post.getProperties());
		infos.put(PostInfoKey.SHARES, post.getShares());
		infos.put(PostInfoKey.SHARES_COUNT, post.getSharesCount());
		infos.put(PostInfoKey.SOURCE, post.getSource());
		infos.put(PostInfoKey.STATUS_TYPE, post.getStatusType());
		if (!postXml.getParentFile().exists())
			postXml.getParentFile().mkdirs();
		Properties props = new Properties();
		props.putAll(dataValidator(infos));
		try (FileOutputStream fos = new FileOutputStream(postXml))
		{
			props.storeToXML(fos, "Represents a post");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return infos;
	}

	public static HashMap<SerializerKey, Object> groupInfo(Group group, FacebookClient fbc, File groupXml)
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
		if (!groupXml.getParentFile().exists())
			groupXml.getParentFile().mkdirs();
		Properties props = new Properties();
		props.putAll(dataValidator(infos));
		try (FileOutputStream fos = new FileOutputStream(groupXml))
		{
			props.storeToXML(fos, "Represents a group");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return infos;
	}

	public static HashMap<SerializerKey, Object> albumInfo(Album album, FacebookClient fcb, File albumXml, long maxPics)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		if (album == null)
			return infos;
		infos.put(AlbumInfoKey.COUNT, album.getCount());
		infos.put(AlbumInfoKey.COVER_PHOTO_ID, album.getCoverPhoto());
		infos.put(AlbumInfoKey.CREATED, album.getCreatedTime());
		infos.put(AlbumInfoKey.DESCRIPTION, album.getDescription());
		infos.put(AlbumInfoKey.PHOTO_DIR, "./photos");
		infos.put(AlbumInfoKey.PHOTO_INFO, "./photoinfo.xml");
		infos.put(AlbumInfoKey.LAST_UPDATE, album.getUpdatedTime());
		infos.put(AlbumInfoKey.ORIGINAL_LINK, album.getLink());
		infos.put(AlbumInfoKey.PRIVACY, album.getPrivacy());
		infos.put(AlbumInfoKey.NAME, album.getName());
		infos.put(AlbumInfoKey.COMES_FROM, album.getFrom());
		infos.put(AlbumInfoKey.LOCATION, album.getLocation());
		infos.put(AlbumInfoKey.ID, album.getId());
		File dir = albumXml.getParentFile();
		Connection<Photo> photosConn = fcb.fetchConnection(album.getId() + "/photos", Photo.class);
		System.out.print("Fetching Photos from " + album.getName() + ". This may take a while...\t\t");
		long iterator = 0;
		Iterator<List<Photo>> it = photosConn.iterator();
		boolean breakLoop = false;
		if (!albumXml.getParentFile().exists())
			albumXml.getParentFile().mkdirs();
		File albumPhotos = FileUtils.resolveRelativePath(albumXml.getParentFile(), infos.get(AlbumInfoKey.PHOTO_DIR).toString());
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
				File photoDir = new File("" + albumPhotos + SDO.SLASH + photo.getId());
				if (!photoDir.exists())
					photoDir.mkdirs();
				File photoXml = FileUtils.resolveRelativePath(photoDir, infos.get(AlbumInfoKey.PHOTO_INFO).toString());
				photoInfo(photo, photoXml);
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
			pros.storeToXML(new FileOutputStream(albumXml), "Info about album " + album.getId());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infos;
	}

	public static HashMap<SerializerKey, Object> photoInfo(Photo photo, File photoXml)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		if (photo == null)
			return infos;
		infos.put(PhotoInfoKey.FILE, "./photo.jpg");
		infos.put(PhotoInfoKey.BACK_DATE, (photo.getBackdatedTime() == null) ? null : photo.getBackdatedTime());
		infos.put(PhotoInfoKey.COMMENT_DIR, "./comments");
		infos.put(PhotoInfoKey.COMMENT_INFO_FILENAME, "./commentinfo.xml");
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
		try (FileOutputStream fos = new FileOutputStream(photoXml))
		{
			props.storeToXML(fos, "Infos about photo " + photo.getId());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File commentsDir = FileUtils.resolveRelativePath(photoXml.getParentFile(), infos.get(PhotoInfoKey.COMMENT_DIR).toString());
		for (Comment comment : photo.getComments())
		{
			File commentDir = new File("" + commentsDir + SDO.SLASH + comment.getId());
			commentDir.mkdirs();
			File commentXml = FileUtils.resolveRelativePath(commentDir, infos.get(PhotoInfoKey.COMMENT_INFO_FILENAME).toString());
			commentInfo(comment, commentXml);
		}
		downloadPhoto(photo, FileUtils.resolveRelativePath(photoXml.getParentFile(), infos.get(PhotoInfoKey.FILE).toString()));
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

	public static HashMap<SerializerKey, Object> pageInfo(Page page, File pageXml)
	{
		HashMap<SerializerKey, Object> infos = new HashMap<>();
		infos.put(PageInfoKey.ABOUT, page.getAbout());
		infos.put(PageInfoKey.AFFILIATION, page.getAffiliation());
		infos.put(PageInfoKey.ARTISTS_WE_LIKE, page.getArtistsWeLike());
		infos.put(PageInfoKey.AWARDS, page.getAwards());
		infos.put(PageInfoKey.BAND_INTERESTS, page.getBandInterests());
		infos.put(PageInfoKey.BAND_MEMBERS, page.getBandMembers());
		infos.put(PageInfoKey.BEST_PAGE, page.getBestPage());
		infos.put(PageInfoKey.BIO, page.getBio());
		infos.put(PageInfoKey.BIRTHDAY, page.getBirthday());
		infos.put(PageInfoKey.BOOKING_AGENT, page.getBookingAgent());
		infos.put(PageInfoKey.BUILT, page.getBuilt());
		infos.put(PageInfoKey.BUISSNESS, page.getBusiness());
		infos.put(PageInfoKey.CATEGORIES, page.getCategoryList());
		infos.put(PageInfoKey.CATEGORY, page.getCategory());
		infos.put(PageInfoKey.CHECKINS, page.getCheckins());
		infos.put(PageInfoKey.COMPANY_OVERVIEW, page.getCompanyOverview());
		infos.put(PageInfoKey.CONTACT_ADRESS, page.getContactAddress());
		infos.put(PageInfoKey.COVER, page.getCover());
		infos.put(PageInfoKey.CULINARY_TEAM, page.getCulinaryTeam());
		infos.put(PageInfoKey.CURRENT_LOCATION, page.getCurrentLocation());
		infos.put(PageInfoKey.DESCRIPTION, page.getDescription());
		infos.put(PageInfoKey.DESCRIPTION_HTML, page.getDescriptionHtml());
		infos.put(PageInfoKey.DIRECTOR, page.getDirectedBy());
		infos.put(PageInfoKey.DRESSCODE, page.getAttire());
		infos.put(PageInfoKey.EMAILS, page.getEmails());
		infos.put(PageInfoKey.ENGAGEMENT, page.getEngagement());
		infos.put(PageInfoKey.FEATURED_VIDEO, page.getFeaturedVideo());
		infos.put(PageInfoKey.FEATURES, page.getFeatures());
		infos.put(PageInfoKey.FOOD_STYLES, page.getFoodStyles());
		infos.put(PageInfoKey.FOUNDED, page.getFounded());
		infos.put(PageInfoKey.GENERAINFO, page.getGeneralInfo());
		infos.put(PageInfoKey.GENERAL_MANAGER, page.getGeneralManager());
		infos.put(PageInfoKey.GENRE, page.getGenre());
		infos.put(PageInfoKey.GLOBAL_BRAND_PAGE_NAME, page.getGlobalBrandPageName());
		infos.put(PageInfoKey.GLOBAL_PARENT_PAGE, page.getGlobalBrandParentPage());
		infos.put(PageInfoKey.HOMETOWN, page.getHometown());
		infos.put(PageInfoKey.HOURS, page.getHours());
		infos.put(PageInfoKey.ID, page.getId());
		infos.put(PageInfoKey.IMPRESSUM, page.getImpressum());
		infos.put(PageInfoKey.INFLUENCES, page.getInfluences());
		infos.put(PageInfoKey.IS_COMMUNITY_PAGE, page.getIsCommunityPage());
		infos.put(PageInfoKey.IS_PERMANENTLY_CLOSED, page.getIsPermanentlyClosed());
		infos.put(PageInfoKey.IS_PUBLISHED, page.getIsPublished());
		infos.put(PageInfoKey.IS_UNCLAIMED, page.getIsUnclaimed());
		infos.put(PageInfoKey.IS_VERIFIED, page.getIsVerified());
		infos.put(PageInfoKey.LIKES, page.getLikes());
		infos.put(PageInfoKey.LINK, page.getLink());
		infos.put(PageInfoKey.MEMBERS, page.getMembers());
		infos.put(PageInfoKey.MISSION, page.getMission());
		infos.put(PageInfoKey.MPG, page.getMpg());
		infos.put(PageInfoKey.NAME, page.getName());
		infos.put(PageInfoKey.NETWORK, page.getNetwork());
		infos.put(PageInfoKey.NEW_LIKE_COUNT, page.getNewLikeCount());
		infos.put(PageInfoKey.PAYMENT_OPTIONS, page.getPaymentOptions());
		infos.put(PageInfoKey.PERSONAL_INFO, page.getPersonalInfo());
		infos.put(PageInfoKey.PHARMA_SAFETY_INFO, page.getPharmaSafetyInfo());
		infos.put(PageInfoKey.PHONE, page.getPhone());
		infos.put(PageInfoKey.PICTURE, page.getPicture());
		infos.put(PageInfoKey.PLOT_OUTLINE, page.getPlotOutline());
		infos.put(PageInfoKey.PRESS_CONTACT, page.getPressContact());
		infos.put(PageInfoKey.PRICE_RANGE, page.getPriceRange());
		infos.put(PageInfoKey.PRODUCTS, page.getProducts());
		infos.put(PageInfoKey.PROPDUCER, page.getProducedBy());
		infos.put(PageInfoKey.PUBLIC_TRANSIT, page.getPublicTransit());
		infos.put(PageInfoKey.RECORD_LABEL, page.getRecordLabel());
		infos.put(PageInfoKey.RELEASE_DATE, page.getReleaseDate());
		infos.put(PageInfoKey.RESTAURANT_SERVICES, page.getRestaurantServices());
		infos.put(PageInfoKey.RESTAURANT_SPECIALITIES, page.getRestaurantSpecialties());
		infos.put(PageInfoKey.SCHEDULE, page.getSchedule());
		infos.put(PageInfoKey.SCREENPLAY_BY, page.getScreenplayBy());
		infos.put(PageInfoKey.SEASON, page.getSeason());
		infos.put(PageInfoKey.STARRING, page.getStarring());
		infos.put(PageInfoKey.START_INFO, page.getStartInfo());
		infos.put(PageInfoKey.STORE_NUMBER, page.getStoreNumber());
		infos.put(PageInfoKey.STUDIO, page.getStudio());
		infos.put(PageInfoKey.TALKING_ABOUT, page.getTalkingAboutCount());
		infos.put(PageInfoKey.UNREAD_MESSAGES, page.getUnreadMessageCount());
		infos.put(PageInfoKey.UNREAD_NOTIFICATIONS, page.getUnreadNotifCount());
		infos.put(PageInfoKey.UNSEEN_MESSAGES, page.getUnseenMessageCount());
		infos.put(PageInfoKey.USERNAME, page.getUsername());
		infos.put(PageInfoKey.VOIP_INFO, page.getVoipInfo());
		infos.put(PageInfoKey.WEBSITE, page.getWebsite());
		infos.put(PageInfoKey.WERE_HRER, page.getWereHereCount());
		infos.put(PageInfoKey.WRITTEN_BY, page.getWrittenBy());
		if (!pageXml.getParentFile().exists())
			pageXml.getParentFile().mkdirs();
		Properties props = new Properties();
		props.putAll(dataValidator(infos));
		try (FileOutputStream fos = new FileOutputStream(pageXml))
		{
			props.storeToXML(fos, "Represents a page");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return infos;
	}

	public static void commentInfo(Comment comment, File commentXml)
	{
		if (!commentXml.getParentFile().exists())
			commentXml.getParentFile().mkdirs();
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
		try (FileOutputStream fos = new FileOutputStream(commentXml))
		{
			props.storeToXML(fos, "Represents a comment");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if (comment.getComments() != null)
			for (Comment c : comment.getComments().getData())
				commentInfo(c, new File("" + commentXml + SDO.SLASH + c.getId()));
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
