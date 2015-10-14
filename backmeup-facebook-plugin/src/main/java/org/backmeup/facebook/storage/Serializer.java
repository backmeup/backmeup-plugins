/**
 * @author Stoeckl R.
 */

package org.backmeup.facebook.storage;

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
import java.util.Map;
import java.util.Properties;

import org.backmeup.facebook.FacebookDatasource;
import org.backmeup.facebook.PropertyFile;
import org.backmeup.facebook.storage.keys.AlbumInfoKey;
import org.backmeup.facebook.storage.keys.CommentKey;
import org.backmeup.facebook.storage.keys.GroupInfoKey;
import org.backmeup.facebook.storage.keys.PageInfoKey;
import org.backmeup.facebook.storage.keys.PhotoInfoKey;
import org.backmeup.facebook.storage.keys.PostInfoKey;
import org.backmeup.facebook.storage.keys.SerializerKey;
import org.backmeup.facebook.storage.keys.UserInfoKey;
import org.backmeup.facebook.utils.CustomStringBuilder;
import org.backmeup.facebook.utils.FileUtils;
import org.backmeup.plugin.api.Progressable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.gagawa.java.elements.A;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.experimental.api.Facebook;
import com.restfb.types.Album;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.Group;
import com.restfb.types.Location;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Page;
import com.restfb.types.Photo;
import com.restfb.types.Photo.Image;
import com.restfb.types.Photo.Tag;
import com.restfb.types.Post;
import com.restfb.types.Post.Privacy;
import com.restfb.types.User;
import com.restfb.types.User.Currency;

public class Serializer {
    private static final String LONELY_ALBUM_ID = "lonely";
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookDatasource.class);

    private Serializer() {
    }

    public static void generateAll(FacebookClient fbc, File coreFile, List<String> skipAlbums, long maxAlbumpics, Progressable progress)
            throws IOException {
        generateAll(fbc, new Facebook(fbc), coreFile, skipAlbums, maxAlbumpics, progress);
    }

    public static void generateAll(FacebookClient fbc, Facebook facebook, File dir, List<String> skipAlbums, long maxAlbumpics,
            Progressable progress) throws IOException {
        CustomStringBuilder builder = new CustomStringBuilder("|");
        Properties listProps = new Properties();

        String path = "user.xml";
        User user = fbc.fetchObject("me", User.class, MasterParameter.getParameterByClass(User.class));
        userInfo(user, true, true, new File(dir, path));
        listProps.put(PropertyFile.USER.toString(), path);

        // the Graph API only returns friends who are using this app, so it is useless to fetch them

        Connection<Album> albums = fbc.fetchConnection("me/albums", Album.class, MasterParameter.getParameterByClass(Album.class),
                Parameter.with("limit", 200));
        List<Album> albumList = new ArrayList<>(albums.getData());

        Connection<Photo> photos = fbc.fetchConnection("me/photos", Photo.class, MasterParameter.getParameterByClass(Photo.class),
                Parameter.with("limit", 200));
        Album lonelyAlbum = new Album();
        lonelyAlbum.setName("Einzelbilder");
        lonelyAlbum.setDescription("Fotos ohne Album");
        if (!photos.getData().isEmpty()) {
            lonelyAlbum.setCoverPhoto(photos.getData().get(0).getId());
        }
        lonelyAlbum.setId(LONELY_ALBUM_ID);
        albumList.add(lonelyAlbum);

        for (Album album : albumList) {
            if (!skipAlbums.contains(album.getName())) {
                path = "albums" + File.separator + album.getId() + File.separator + "albuminfo.xml";
                builder.append(path);
                albumInfo(album, fbc, new File(dir, path), maxAlbumpics, LONELY_ALBUM_ID.equalsIgnoreCase(album.getId()), photos, progress);
            }
        }
        listProps.put(PropertyFile.ALBUMS.toString(), builder.toString());
        builder.empty();

        Connection<Post> posts = fbc.fetchConnection("me/posts", Post.class, MasterParameter.getParameterByClass(Post.class),
                Parameter.with("limit", 200));
        //Info: RestFB supports paging, everytime 25 elements are requested with a cursor where to receive the next ones
        for (Post post : posts.getData()) {
            path = "posts" + File.separator + post.getId() + File.separator + "postinfo.xml";
            builder.append(path);
            postInfo(post, new File(dir, path), fbc);
        }
        listProps.put(PropertyFile.POSTS.toString(), builder.toString());
        builder.empty();

        for (String pageToken : facebook.pages().fetchAllAccessTokens().values()) {
            FacebookClient fc = new DefaultFacebookClient(pageToken, Version.VERSION_2_3);
            Page page = fc.fetchObject("me", Page.class, MasterParameter.getParameterByClass(Page.class));
            path = "pages" + File.separator + page.getId() + File.separator + "pageinfo.xml";
            builder.append(path);
            pageInfo(page, new File(dir, path));
        }
        listProps.put(PropertyFile.PAGES.toString(), builder.toString());
        builder.empty();

        Connection<Group> groups = fbc.fetchConnection("me/groups", Group.class, MasterParameter.getParameterByClass(Group.class));
        for (Group group : groups.getData()) {
            path = "groups" + File.separator + group.getId() + File.separator + "groupinfo.xml";
            builder.append(path);
            groupInfo(group, new File(dir, path));
        }
        listProps.put(PropertyFile.GROUPS.toString(), builder.toString());

        writeProperties(listProps, new File(dir, "core.xml"), "list of all xml files");
    }

    public static Map<SerializerKey, Object> userInfo(User user, boolean thisIsMe, boolean makeDirs, File userXml) throws IOException {
        Map<SerializerKey, Object> infos = new HashMap<SerializerKey, Object>();
        if (user == null) {
            return infos;
        }
        if (!userXml.getParentFile().exists()) {
            userXml.getParentFile().mkdirs();
        }

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

        writeInfos(infos, userXml, "Represents a user");
        return infos;
    }

    public static Map<SerializerKey, Object> postInfo(Post post, File postXml, FacebookClient fbc) throws IOException {
        Map<SerializerKey, Object> infos = new HashMap<>();
        if (post == null || postXml == null) {
            return infos;
        }

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

        File photoXml = null;
        if (post.getObjectId() != null) {
            try {
                Photo photo = fbc.fetchObject(post.getObjectId(), Photo.class, MasterParameter.getParameterByClass(Photo.class));
                String photoXmlPath = "object" + File.separator + "photoinfo.xml";
                photoXml = new File(postXml.getParentFile(), photoXmlPath);
                photoInfo(photo, photoXml, fbc);
                infos.put(PostInfoKey.PICTURE, photoXmlPath);
            } catch (FacebookOAuthException e) {
                LOGGER.error("error getting photo object to post with id:" + post.getObjectId(), e);
            }
        }

        // this is the proper way of requesting comments + sub-comments
        Connection<Comment> commentConnection = fbc.fetchConnection(post.getId() + "/comments", Comment.class,
                MasterParameter.getByClass(Comment.class).getParameter(), Parameter.with("limit", 200));

        for (List<Comment> comments : commentConnection) {
            //update the nr of received comments
            infos.put(PostInfoKey.COMMENTS_COUNT, comments.size());
            for (Comment comment : comments) {
                commentInfo(comment, new File(postXml.getParentFile(), "comments" + File.separator + comment.getId() + File.separator
                        + "commentinfo.xml"), fbc);
            }
        }

        infos.put(PostInfoKey.PLACE, post.getPlace());
        infos.put(PostInfoKey.PRIVACY, post.getPrivacy());
        infos.put(PostInfoKey.PROPERTIES, post.getProperties());
        infos.put(PostInfoKey.SHARES, post.getShares());
        infos.put(PostInfoKey.SHARES_COUNT, post.getSharesCount());
        infos.put(PostInfoKey.SOURCE, post.getSource());
        infos.put(PostInfoKey.STATUS_TYPE, post.getStatusType());

        writeInfos(infos, postXml, "Represents a post");
        return infos;
    }

    public static Map<SerializerKey, Object> groupInfo(Group group, File groupXml) throws IOException {
        Map<SerializerKey, Object> infos = new HashMap<>();
        if (group == null) {
            return infos;
        }

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

        writeInfos(infos, groupXml, "Represents a group");
        return infos;
    }

    public static Map<SerializerKey, Object> albumInfo(Album album, FacebookClient fcb, File albumXml, long maxPics, boolean fakeAlbum,
            Connection<Photo> fakePhotos, Progressable progress) throws IOException {
        Map<SerializerKey, Object> infos = new HashMap<>();
        if (album == null) {
            return infos;
        }

        infos.put(AlbumInfoKey.COUNT, album.getCount());
        infos.put(AlbumInfoKey.COVER_PHOTO_ID, album.getCoverPhoto());
        infos.put(AlbumInfoKey.CREATED, album.getCreatedTime());
        infos.put(AlbumInfoKey.DESCRIPTION, album.getDescription());
        infos.put(AlbumInfoKey.PHOTO_DIR, "photos");
        infos.put(AlbumInfoKey.PHOTO_INFO, "photoinfo.xml");
        infos.put(AlbumInfoKey.LAST_UPDATE, album.getUpdatedTime());
        infos.put(AlbumInfoKey.ORIGINAL_LINK, album.getLink());
        infos.put(AlbumInfoKey.PRIVACY, album.getPrivacy());
        infos.put(AlbumInfoKey.NAME, album.getName());
        infos.put(AlbumInfoKey.COMES_FROM, album.getFrom());
        infos.put(AlbumInfoKey.LOCATION, album.getLocation());
        infos.put(AlbumInfoKey.ID, album.getId());

        File dir = albumXml.getParentFile();
        Connection<Photo> photosConn = null;
        if (fakeAlbum && fakePhotos != null) {
            photosConn = fakePhotos;
        } else {
            photosConn = fcb.fetchConnection(album.getId() + "/photos", Photo.class, MasterParameter.getParameterByClass(Photo.class),
                    Parameter.with("limit", 200));
        }
        if (progress != null) {
            progress.progress("Fetching " + (album.getCount() != null && (maxPics > album.getCount()) ? album.getCount() : maxPics)
                    + " photos from " + album.getName() + ". This may take a while...");
        }

        long iterator = 0;
        Iterator<List<Photo>> it = photosConn.iterator();
        if (!albumXml.getParentFile().exists()) {
            albumXml.getParentFile().mkdirs();
        }
        File albumPhotos = FileUtils.resolveRelativePath(albumXml.getParentFile(), infos.get(AlbumInfoKey.PHOTO_DIR).toString());
        outer: while (it.hasNext()) {
            List<Photo> photos = it.next();
            for (Photo photo : photos) {
                if (iterator >= maxPics && maxPics >= 0) {
                    break outer;
                }
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File photoDir = new File(albumPhotos, photo.getId());
                if (!photoDir.exists()) {
                    photoDir.mkdirs();
                }
                File photoXml = FileUtils.resolveRelativePath(photoDir, infos.get(AlbumInfoKey.PHOTO_INFO).toString());
                photoInfo(photo, photoXml, fcb);
                iterator++;
            }
        }

        infos.put(AlbumInfoKey.LOCAL_COUNT, iterator);
        writeInfos(infos, albumXml, "Represents an album");
        return infos;
    }

    public static Map<SerializerKey, Object> photoInfo(Photo photo, File photoXml, FacebookClient fbc) throws IOException {
        Map<SerializerKey, Object> infos = new HashMap<>();
        if (photo == null) {
            return infos;
        }

        infos.put(PhotoInfoKey.FILE, "photo.jpg");
        infos.put(PhotoInfoKey.BACK_DATE, (photo.getBackdatedTime() == null) ? null : photo.getBackdatedTime());
        infos.put(PhotoInfoKey.COMMENT_DIR, "comments");
        infos.put(PhotoInfoKey.COMMENT_INFO_FILENAME, "commentinfo.xml");
        infos.put(PhotoInfoKey.LAST_UPDATE, photo.getUpdatedTime());
        infos.put(PhotoInfoKey.ORIGINAL_LINK, photo.getLink());
        infos.put(PhotoInfoKey.LIKES_FROM_PEOPLE, photo.getLikes());
        infos.put(PhotoInfoKey.LIKES, photo.getLikes().size());
        infos.put(PhotoInfoKey.LOCATION, (photo.getPlace() != null) ? photo.getPlace().getLocation() : null);
        infos.put(PhotoInfoKey.PLACE, photo.getPlace());
        infos.put(PhotoInfoKey.PUBLISH_DATE, photo.getCreatedTime());
        infos.put(PhotoInfoKey.ID, photo.getId());
        infos.put(PhotoInfoKey.FROM, photo.getFrom());
        infos.put(PhotoInfoKey.TAGS, photo.getTags());
        infos.put(PhotoInfoKey.ICON, photo.getIcon());
        infos.put(PhotoInfoKey.HEIGHT, photo.getHeight());
        infos.put(PhotoInfoKey.IMAGES, photo.getImages());
        infos.put(PhotoInfoKey.NAME, photo.getName());
        infos.put(PhotoInfoKey.PICTURE, photo.getPicture());
        infos.put(PhotoInfoKey.WIDTH, photo.getWidth());
        writeInfos(infos, photoXml, "Represents a photo");

        File commentsDir = FileUtils.resolveRelativePath(photoXml.getParentFile(), infos.get(PhotoInfoKey.COMMENT_DIR).toString());
        for (Comment cmt : photo.getComments()) {

            Comment comment = fbc.fetchObject(cmt.getId(), Comment.class, MasterParameter.getParameterByClass(Comment.class));
            File commentDir = new File(commentsDir, comment.getId());
            commentDir.mkdirs();
            File commentXml = FileUtils.resolveRelativePath(commentDir, infos.get(PhotoInfoKey.COMMENT_INFO_FILENAME).toString());
            commentInfo(comment, commentXml, fbc);
        }

        downloadPhoto(photo, FileUtils.resolveRelativePath(photoXml.getParentFile(), infos.get(PhotoInfoKey.FILE).toString()));
        return infos;
    }

    public static void downloadPhoto(Photo photo, File out) {
        String url = "";
        Integer lastWidth = 0;
        Integer lastHeight = 0;
        for (Image img : photo.getImages()) {
            if (img.getWidth() >= lastWidth && img.getHeight() >= lastHeight) {
                lastWidth = img.getWidth();
                lastHeight = img.getHeight();
                url = img.getSource();
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileOutputStream fw = new FileOutputStream(out);
                BufferedInputStream br = new BufferedInputStream(new URL(url).openStream());) {
            byte[] puffer = new byte[4096];
            int i = 0;
            while ((i = br.read(puffer)) != -1) {
                baos.write(puffer, 0, i);
            }
            byte[] result = baos.toByteArray();
            fw.write(result);
        } catch (IOException e) {
            LOGGER.error("cannot download photo", e);
        }
    }

    public static Map<SerializerKey, Object> pageInfo(Page page, File pageXml) throws IOException {
        Map<SerializerKey, Object> infos = new HashMap<>();

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
        infos.put(PageInfoKey.LOCATION, page.getLocation());
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

        writeInfos(infos, pageXml, "Represents a page");
        return infos;
    }

    public static void commentInfo(Comment comment, File commentXml, FacebookClient fbc) throws IOException {
        if (!commentXml.getParentFile().exists()) {
            commentXml.getParentFile().mkdirs();
        }
        Map<SerializerKey, Object> infos = new HashMap<>();

        File photoJpg = null;
        if ((comment.getAttachment() != null) && (comment.getAttachment().getMedia() != null)
                && (comment.getAttachment().getMedia().getImage() != null)) {
            Comment.Image cimg = comment.getAttachment().getMedia().getImage();
            Photo photo = new Photo();
            photo.setId("attachment");
            Photo.Image img = new Image();
            img.setSource(cimg.getSrc());
            img.setHeight(cimg.getHeight());
            img.setWidth(cimg.getWidth());
            photo.addImage(img);
            String photoJpgPath = "attachment" + File.separator + "photoinfo.xml";
            photoJpg = new File(commentXml.getParentFile(), photoJpgPath);
            photoInfo(photo, photoJpg, fbc);
            infos.put(CommentKey.ATTACHMENT, photoJpgPath);
        }

        infos.put(CommentKey.CAN_REMOVE, comment.getCanRemove());
        infos.put(CommentKey.CREATED, comment.getCreatedTime());
        infos.put(CommentKey.FROM, comment.getFrom());
        infos.put(CommentKey.HIDDEN, comment.getIsHidden());
        infos.put(CommentKey.ID, comment.getId());
        infos.put(CommentKey.LIKE_COUNT, comment.getLikeCount());
        infos.put(CommentKey.MESSAGE, comment.getMessage());
        infos.put(CommentKey.REPLIES_COUNT, comment.getCommentCount()); //always zero
        infos.put(CommentKey.METADATA, comment.getMetadata());

        writeInfos(infos, commentXml, "Represents a comment");

        //check for sub-comments
        Connection<Comment> subCommentsConnection = fbc.fetchConnection(comment.getId() + "/comments", Comment.class,
                Parameter.with("limit", 50));
        if (subCommentsConnection != null) {
            for (List<Comment> subcomments : subCommentsConnection) {
                infos.put(CommentKey.REPLIES_COUNT, subcomments.size());
                for (Comment subcomment : subcomments) {
                    commentInfo(subcomment, new File(commentXml.getParentFile(), subcomment.getId() + "/commentinfo.xml"), fbc);
                }
            }
        }
    }

    public static Properties getReadableUserInfos(Map<SerializerKey, Object> userInfos) {
        Properties infos = new Properties();
        if (userInfos == null) {
            return infos;
        }

        for (Map.Entry<SerializerKey, Object> entry : userInfos.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                infos.put(entry.getKey().toString(), value.toString());
            }
        }

        return infos;
    }

    public static Properties dataValidator(Map<SerializerKey, Object> map) {
        Properties props = new Properties();
        if (map == null) {
            return props;
        }

        for (Map.Entry<SerializerKey, Object> entry : map.entrySet()) {
            SerializerKey key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && key.getType() != null) {
                String stringKey = key.toString();
                String stringValue = null;

                switch (key.getType()) {
                case OTHER: {
                    stringValue = packList(value);
                    break;
                }
                case DATE: {
                    if (value instanceof Date) {
                        stringValue = "" + ((Date) value).getTime();
                    }
                    break;
                }
                case LIST: {
                    if (value instanceof List<?>) {
                        stringValue = packList(value);
                    }
                    break;
                }
                case NFT: {
                    if (value instanceof NamedFacebookType) {
                        stringValue = ((NamedFacebookType) value).getName();
                    }
                    break;
                }
                case CFT: {
                    if (value instanceof CategorizedFacebookType) {
                        stringValue = ((CategorizedFacebookType) value).getName();
                    }
                    break;
                }
                default: {
                    stringValue = value.toString();
                    break;
                }
                }

                if (stringValue != null) {
                    props.setProperty(stringKey, stringValue);
                }
            }
        }

        return props;
    }

    public static String packList(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof List<?>) {
            CustomStringBuilder sb = new CustomStringBuilder(";");
            List<?> list = (List<?>) object;
            for (Object o : list) {
                sb.append(packList(o));
            }
            return sb.toString();
        } else if (object instanceof Location) {
            Location loc = (Location) object;
            A a = new A();
            a.setHref("https://www.google.at/maps/@" + loc.getLatitude() + "," + loc.getLongitude() + ",17z");
            a.appendText(loc.getStreet() + ", " + loc.getCity() + ", " + loc.getState() + ", " + loc.getCountry());
            return a.write();
        } else if (object instanceof Privacy) {
            return ((Privacy) object).getDescription();
        } else if (object instanceof Currency) {
            return ((Currency) object).getUserCurrency();
        } else if (object instanceof NamedFacebookType) {
            return ((NamedFacebookType) object).getName();
        } else if (object instanceof Image) {
            Image img = (Image) object;
            A a = new A();
            a.setHref(img.getSource());
            a.appendText(img.getWidth() + " x " + img.getHeight());
            return a.write(); // + ", ";
        } else if (object instanceof CategorizedFacebookType) {
            return ((CategorizedFacebookType) object).getName();
        } else if (object instanceof Tag) {
            Tag t = (Tag) object;
            return "X: " + t.getX() + "Y: " + t.getY();
        } else {
            return object.toString();
        }
    }

    public static String genLikes(List<NamedFacebookType> likes) {
        if (likes == null) {
            return "";
        }

        CustomStringBuilder sb = new CustomStringBuilder(";");
        for (NamedFacebookType nft : likes) {
            sb.append(nft.getName());
        }
        return sb.toString();
    }

    public static void writeInfos(Map<SerializerKey, Object> infos, File xml, String comment) throws IOException {
        writeProperties(dataValidator(infos), xml, comment);
    }

    public static void writeProperties(Properties props, File xml, String comment) throws IOException {
        if (!xml.getParentFile().exists()) {
            xml.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(xml);) {
            props.storeToXML(fos, comment);
        }
    }
}
