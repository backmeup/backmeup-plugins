/**
* @author Richard Stöckl
*/

package org.backmeup.facebook.htmlgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.backmeup.facebook.files.ConfLoader;
import org.backmeup.facebook.files.PropertyFile;
import org.backmeup.facebook.storage.Datatype;
import org.backmeup.facebook.storage.keys.AlbumInfoKey;
import org.backmeup.facebook.storage.keys.CommentKey;
import org.backmeup.facebook.storage.keys.GroupInfoKey;
import org.backmeup.facebook.storage.keys.PageInfoKey;
import org.backmeup.facebook.storage.keys.PhotoInfoKey;
import org.backmeup.facebook.storage.keys.PostInfoKey;
import org.backmeup.facebook.storage.keys.SerializerKey;
import org.backmeup.facebook.storage.keys.UserInfoKey;
import org.backmeup.facebook.utils.FileUtils;

import com.hp.gagawa.java.Document;
import com.hp.gagawa.java.DocumentType;
import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.H1;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Li;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.P;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Title;
import com.hp.gagawa.java.elements.Tr;
import com.hp.gagawa.java.elements.Ul;

public class HTMLGenerator
{
	public void genOverview(File root, File dirProps)
	{
		Document index = new Document(DocumentType.HTMLTransitional);
		Properties dirs = loadProperties(dirProps);
		File target = FileUtils.resolveRelativePath(dirProps.getParentFile(), dirs.getProperty(PropertyFile.USER.toString()));
		Properties userProps = loadProperties(target);
		File indexTarget = new File("" + root + "/index.html");
		initDocumentHeader(index, "Ihr Account", indexTarget, null, root, true);
		File groups = new File("" + root + "/groups/groups.html");
		ArrayList<File> groupXmls = new ArrayList<>();
		for (String s : dirs.getProperty(PropertyFile.GROUPS.toString()).split("\\|"))
		{
			File singleFile = FileUtils.resolveRelativePath(dirProps.getParentFile(), s);
			groupXmls.add(singleFile);
		}
		groupOverview(groups, root, groupXmls);
		ArrayList<File> postFiles = new ArrayList<>();
		for (String s : dirs.getProperty(PropertyFile.POSTS.toString()).split("\\|"))
			postFiles.add(FileUtils.resolveRelativePath(dirProps.getParentFile(), s));
		genPosts(postFiles, new File("" + root + "/posts.html"), root);
		ArrayList<File> pageFiles = new ArrayList<>();
		for (String s : dirs.getProperty(PropertyFile.PAGES.toString()).split("\\|"))
			pageFiles.add(FileUtils.resolveRelativePath(dirProps.getParentFile(), s));
		genPages(new File("" + root + "/pages/pages.html"), pageFiles, root);
		initDocumentHeader(index, userProps.getProperty(UserInfoKey.FIRST_NAME.toString()), indexTarget, null, root, false);
		Div sideInfos = new Div();
		sideInfos.setCSSClass("sidebar");
		sideInfos.appendChild(wrapInfos(UserInfoKey.values(), userProps, true));
		index.body.appendChild(sideInfos);
		ArrayList<File> albumFiles = new ArrayList<>();
		for (String s : dirs.getProperty(PropertyFile.ALBUMS.toString()).split("\\|"))
			albumFiles.add(FileUtils.resolveRelativePath(dirProps, s));
		genAlbums(new File("" + root + "/albums/albums.html"), albumFiles, root);
		writeDocument(index, indexTarget);
	}

	private void genAlbums(File albumsHtml, ArrayList<File> albumsPropsFiles, File root)
	{
		File albumFolder = albumsHtml.getParentFile();
		if (!albumFolder.exists())
			albumFolder.mkdirs();
		Document albums = new Document(DocumentType.HTMLTransitional);
		albums = initDocumentHeader(albums, "Alben", albumsHtml, null, root, true);
		Div albumContainer = new Div();
		albumContainer.setCSSClass("picture_container");
		Ul albumlist = new Ul();
		for (File albumProps : albumsPropsFiles)
		{
			Properties albumsProps = loadProperties(albumProps);
			Div innerItem = new Div();
			Li item = new Li();
			String relativeImg = "";
			File photoFolder = FileUtils.resolveRelativePath(albumProps, albumsProps.getProperty(AlbumInfoKey.PHOTO_DIR.toString()));
			File coverDir = new File("" + photoFolder + "/" + albumsProps.getProperty(AlbumInfoKey.COVER_PHOTO_ID.toString()));
			File coverXml = FileUtils.resolveRelativePath(coverDir, albumsProps.getProperty(AlbumInfoKey.PHOTO_INFO.toString()));
			Properties coverProps = loadProperties(coverXml);
			File photo = FileUtils.resolveRelativePath(coverXml, coverProps.getProperty(PhotoInfoKey.FILE.toString()));
			relativeImg = FileUtils.getWayTo(albumsHtml, photo);
			A albumLink = new A();
			Img cover = new Img(albumsProps.getProperty(AlbumInfoKey.NAME.toString()), relativeImg);
			innerItem.setCSSClass("album_picture");
			innerItem.appendChild(cover);
			albumLink.appendChild(innerItem);
			File singleAlbumHtml = new File("" + albumsHtml.getParentFile() + "/" + albumsProps.getProperty(AlbumInfoKey.ID.toString()) + "/album.html");
			albumLink.setHref(FileUtils.getWayTo(albumsHtml, singleAlbumHtml));
			String albumName = albumsProps.getProperty(AlbumInfoKey.NAME.toString());
			String desc = albumsProps.getProperty(AlbumInfoKey.DESCRIPTION.toString());
			P textBelow = new P();
			if (albumName != null)
				textBelow.appendText(albumName);
			if (desc != null)
				textBelow.appendText("<br/>" + desc);
			innerItem.appendChild(textBelow);
			item.appendChild(albumLink);
			albumlist.appendChild(item);
			genAlbum(albumsProps, albumProps, singleAlbumHtml, root);
		}
		albumContainer.appendChild(albumlist);
		albums.body.appendChild(albumContainer);
		writeDocument(albums, albumsHtml);
	}

	private void genPosts(ArrayList<File> postXmls, File out, File root)
	{
		Document postsDoc = new Document(DocumentType.HTMLTransitional);
		initDocumentHeader(postsDoc, "Posts", out, null, root, true);
		for (File postXml : postXmls)
		{
			Properties props = loadProperties(postXml);
			Div singlePost = new Div();
			File photoXml = FileUtils.resolveRelativePath(postXml, props.getProperty(PostInfoKey.PICTURE.toString()));
			Properties picProps = loadProperties(photoXml);
			File picFile = FileUtils.resolveRelativePath(photoXml, picProps.getProperty(PhotoInfoKey.FILE.toString()));
			Img pic = new Img("Photo", FileUtils.getWayTo(out, picFile));
			singlePost.appendChild(pic);
			singlePost.setCSSClass("comment");
			singlePost.appendChild(wrapInfos(PostInfoKey.values(), props, true));
			postsDoc.body.appendChild(singlePost);
		}
		writeDocument(postsDoc, out);
	}

	private void genAlbum(Properties albumProps, File albumXml, File albumHtml, File root)
	{
		Document albumFile = new Document(DocumentType.HTMLTransitional);
		File photoHtmlContainer = albumHtml.getParentFile();
		if (!photoHtmlContainer.exists())
			photoHtmlContainer.mkdirs();
		File coverXml = FileUtils.resolveRelativePath(FileUtils.resolveRelativePath(albumXml, albumProps.getProperty(AlbumInfoKey.PHOTO_DIR.toString()) + "/" + albumProps.getProperty(AlbumInfoKey.COVER_PHOTO_ID.toString())), albumProps.getProperty(AlbumInfoKey.PHOTO_INFO.toString()));
		Properties coverProps = loadProperties(coverXml);
		File cover = FileUtils.resolveRelativePath(coverXml, coverProps.getProperty(PhotoInfoKey.FILE.toString()));
		albumFile = initDocumentHeader(albumFile, albumProps.getProperty(AlbumInfoKey.NAME.toString(), "Album"), albumHtml, cover, root, true);
		Div photoContainer = new Div();
		photoContainer.setCSSClass("picture_container");
		Ul photoList = new Ul();
		for (File photoFolder : FileUtils.resolveRelativePath(albumXml, albumProps.getProperty(AlbumInfoKey.PHOTO_DIR.toString())).listFiles())
		{
			File photoXml = FileUtils.resolveRelativePath(photoFolder, albumProps.getProperty(AlbumInfoKey.PHOTO_INFO.toString()));
			Properties photoProps = loadProperties(photoXml);
			Li photoItem = new Li();
			A photoLink = new A();
			File photoHtml = new File("" + albumHtml.getParentFile() + "/" + photoProps.getProperty(PhotoInfoKey.ID.toString()) + ".html");
			photoLink.setHref(FileUtils.getWayTo(albumHtml, photoHtml));
			Div innerItem = new Div().setCSSClass("album_picture");
			String relativePhoto = FileUtils.getWayTo(albumHtml, FileUtils.resolveRelativePath(photoXml, photoProps.getProperty(PhotoInfoKey.FILE.toString())));
			innerItem.appendChild(new Img("Photo", relativePhoto));
			innerItem.appendChild(new P().appendText("Likes: " + photoProps.getProperty(PhotoInfoKey.LIKES.toString())));
			photoLink.appendChild(innerItem);
			photoItem.appendChild(photoLink);
			photoList.appendChild(photoItem);
			ArrayList<Node> commentNodes = new ArrayList<>();
			File comments = new File("" + photoXml.getParentFile() + "/" + photoProps.getProperty(PhotoInfoKey.COMMENT_DIR.toString()));
			if (comments.exists())
				for (File f : comments.listFiles())
					commentNodes.add(genComment(f, photoHtml));
			genPhotoFile(photoProps, photoHtml, photoXml, root, commentNodes.toArray(new Node[commentNodes.size()]));
		}
		Div sideInfos = new Div();
		sideInfos.setCSSClass("sidebar");
		sideInfos.appendChild(wrapInfos(AlbumInfoKey.values(), albumProps, true));
		photoContainer.appendChild(photoList);
		albumFile.body.appendChild(sideInfos);
		albumFile.body.appendChild(photoContainer);
		writeDocument(albumFile, albumHtml);
	}

	public void groupOverview(File groupHtml, File root, ArrayList<File> groupXmls)
	{
		if (!groupHtml.getParentFile().exists())
			groupHtml.getParentFile().mkdirs();
		Document groupsDoc = new Document(DocumentType.HTMLTransitional);
		initDocumentHeader(groupsDoc, "Gruppen", groupHtml, null, root, true);
		Ul groupList = new Ul();
		for (File groupXml : groupXmls)
		{
			Properties groupProps = loadProperties(groupXml);
			Li groupItem = new Li();
			A groupLink = new A();
			File singleGroupHtml = new File("" + groupHtml.getParentFile() + "/" + groupProps.getProperty(GroupInfoKey.ID.toString()));
			genGroup(groupProps, singleGroupHtml, root);
			groupLink.setHref(FileUtils.getWayTo(groupHtml, singleGroupHtml));
			groupLink.appendText(groupProps.getProperty(GroupInfoKey.NAME.toString()));
			groupItem.appendChild(groupLink);
			groupList.appendChild(groupItem);
		}
		groupsDoc.body.appendChild(groupList);
		writeDocument(groupsDoc, groupHtml);
	}

	public void genGroup(Properties groupProps, File groupHTML, File root)
	{
		if (!groupHTML.getParentFile().exists())
			groupHTML.getParentFile().mkdirs();
		Document groupDoc = new Document(DocumentType.HTMLTransitional);
		initDocumentHeader(groupDoc, groupProps.getProperty(GroupInfoKey.NAME.toString()), groupHTML, null, root, true);
		Div sidebar = new Div();
		sidebar.setCSSClass("sidebar");
		sidebar.appendChild(wrapInfos(GroupInfoKey.values(), groupProps, true));
		groupDoc.body.appendChild(sidebar);
		writeDocument(groupDoc, groupHTML);
	}

	public void genPages(File pagesHtml, ArrayList<File> pageXmls, File root)
	{
		if (!pagesHtml.getParentFile().exists())
			pagesHtml.getParentFile().mkdirs();
		Document pagesDoc = new Document(DocumentType.HTMLTransitional);
		initDocumentHeader(pagesDoc, "Seiten", pagesHtml, null, root, true);
		Ul pageList = new Ul();
		for (File pageXml : pageXmls)
		{
			Li item = new Li();
			A link = new A();
			Properties pageProps = loadProperties(pageXml);
			File pageHtml = new File("" + pagesHtml.getParentFile() + "/" + pageProps.getProperty(PageInfoKey.ID.toString()) + "/page.html");
			link.setHref(FileUtils.getWayTo(pagesHtml, pageHtml));
			link.appendText(pageProps.getProperty(PageInfoKey.NAME.toString()));
			genPage(pageHtml, pageProps, root);
			item.appendChild(link);
			pageList.appendChild(item);
		}
		pagesDoc.body.appendChild(pageList);
		writeDocument(pagesDoc, pagesHtml);
	}

	public void genPage(File pageHtml, Properties pageProps, File root)
	{
		if (!pageHtml.getParentFile().exists())
			pageHtml.getParentFile().mkdirs();
		Document pageDoc = new Document(DocumentType.HTMLTransitional);
		initDocumentHeader(pageDoc, pageProps.getProperty(PageInfoKey.NAME.toString()), pageHtml, null, root, true);
		Div sidebar = new Div();
		sidebar.setCSSClass("sidebar");
		sidebar.appendChild(wrapInfos(PageInfoKey.values(), pageProps, true));
		pageDoc.body.appendChild(sidebar);
		Div realContent = new Div();
		realContent.setCSSClass("page");
		H1 head = new H1();
		head.appendText(pageProps.getProperty(PageInfoKey.NAME.toString()));
		realContent.appendChild(head);
		pageDoc.body.appendChild(realContent);
		writeDocument(pageDoc, pageHtml);
	}

	public Node genComment(File dir, File html)
	{
		Div container = new Div();
		container.setCSSClass("comment");
		container.appendChild(new Br());
		Properties props = loadProperties(new File("" + dir + "/commentinfo.xml"));
		String confImg = props.getProperty(CommentKey.ATTACHMENT.toString());
		if (confImg != null && !confImg.equalsIgnoreCase("null"))
		{
			File img = FileUtils.resolveRelativePath(dir, confImg);
			Properties photoProp = loadProperties(img);
			Img imgTag = new Img("photo", FileUtils.getWayTo(html, FileUtils.resolveRelativePath(img, photoProp.getProperty(PhotoInfoKey.FILE.toString()))));
			container.appendChild(imgTag);
		}
		container.appendChild(wrapInfos(CommentKey.values(), props, true));
		for (File f : dir.listFiles())
			if (f.isDirectory() && f.exists() && !f.getName().equalsIgnoreCase("attachment"))
				container.appendChild(genComment(f, html));
		return container;
	}

	private Table wrapInfos(SerializerKey[] aik, Properties albumsProps, boolean skipinvalid)
	{
		Table table = new Table();
		if (aik.length >= 1 && ConfLoader.reducedInfos())
			aik = aik[0].getReduced();
		for (SerializerKey key : aik)
		{
			String value = albumsProps.getProperty(key.toString());
			if ((value != null && !value.equals("")) || !skipinvalid)
			{
				if (value == null)
					value = "keine Infos vorhanden";
				if (key.getType().equals(Datatype.DATE))
				{
					GregorianCalendar time = new GregorianCalendar();
					Date d = new Date(Long.parseLong(value));
					time.setTime(d);
					value = "am ";
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					value += sdf.format(time.getTime()) + " um ";
					sdf = new SimpleDateFormat("HH:mm:ss");
					value += sdf.format(time.getTime()) + " Uhr";
				}
				if (key.getType().equals(Datatype.LINK))
				{
					A link = new A();
					link.appendText(key.getLabel());
					link.setHref(value);
					value = link.write();
				}
				if (key.getType().equals(Datatype.LIST))
					value = unpackList(Arrays.asList(value.split(";")));
				Tr row = new Tr();
				row.appendChild(new Td().appendText(key.getLabel()));
				row.appendChild(new Td().appendText(value));
				table.appendChild(row);
			}
		}
		return table;
	}

	public static String unpackList(List<?> list)
	{
		if (list == null)
			return "keine Informationen vorhanden";
		StringBuilder sb = new StringBuilder();
		for (Object o : list)
		{
			if (o != null)
			{
				if (o instanceof List<?>)
					sb.append(unpackList((List<?>) o));
				else
					sb.append(o.toString());
				sb.append(", ");
			}
		}
		if (sb.length() > 0)
			sb.delete(sb.length() - 2, sb.length());
		return sb.toString();
	}

	public void genPhotoFile(Properties photoProps, File photoHtml, File photoXml, File root, Node... add)
	{
		File icon = FileUtils.resolveRelativePath(photoXml, photoProps.getProperty(PhotoInfoKey.FILE.toString()));
		Document photoDoc = new Document(DocumentType.HTMLTransitional);
		photoDoc = initDocumentHeader(photoDoc, photoProps.getProperty(PhotoInfoKey.ID.toString(), "Foto"), photoHtml, icon, root, true);
		String relativeImg = FileUtils.getWayTo(photoHtml, icon);
		Img picture = new Img("Photo", relativeImg);
		picture.setCSSClass("picture");
		Div sideInfos = new Div();
		sideInfos.setCSSClass("sidebar");
		sideInfos.appendChild(wrapInfos(PhotoInfoKey.values(), photoProps, true));
		photoDoc.body.appendChild(sideInfos);
		photoDoc.body.appendChild(picture);
		if (add != null)
			for (Node n : add)
				if (n != null)
					photoDoc.body.appendChild(n);
		writeDocument(photoDoc, photoHtml);
	}

	public Node navGenerator(File htmlDir, File root)
	{
		Div container = new Div();
		container.setCSSClass("navContainer");
		Ul menuList = new Ul();
		menuList = appendItem(menuList, htmlDir, new File("" + root + "/index.html"), "Home");
		menuList = appendItem(menuList, htmlDir, new File("" + root + "/albums/albums.html"), "Alben");
		menuList = appendItem(menuList, htmlDir, new File("" + root + "/groups/groups.html"), "Gruppen");
		menuList = appendItem(menuList, htmlDir, new File("" + root + "/posts.html"), "Posts");
		menuList = appendItem(menuList, htmlDir, new File("" + root + "/pages/pages.html"), "Seiten");
		container.appendChild(menuList);
		return container;
	}

	private Ul appendItem(Ul menuList, File from, File to, String Label)
	{
		Li listItem = new Li();
		A link = new A();
		link.setHref(FileUtils.getWayTo(from, to));
		link.appendText(Label);
		listItem.appendChild(link);
		menuList.appendChild(listItem);
		return menuList;
	}

	private Document initDocumentHeader(Document doc, String title, File targetFile, File icon, File root, boolean menuBar, File... cssFiles)
	{
		Meta headMeta = new Meta("content-type");
		headMeta.setAttribute("charset", "utf8");
		headMeta.setAttribute("content", "text/html");
		doc.head.appendChild(headMeta);
		doc.head.appendChild(new Title().appendText(title));
		Link iconLink = new Link();
		iconLink.setType("image/jpg");
		iconLink.setHref(FileUtils.getWayTo(targetFile.getParentFile(), icon));
		iconLink.setRel("icon");
		doc.head.appendChild(iconLink);
		ArrayList<File> instantcssFiles = new ArrayList<>();
		instantcssFiles.add(new File("" + root + "/menu.css"));
		instantcssFiles.add(new File("" + root + "/main.css"));
		ArrayList<File> revList = new ArrayList<>();
		if (cssFiles != null)
			revList = new ArrayList<>(Arrays.asList(cssFiles));
		revList.addAll(instantcssFiles);
		for (File cssFile : revList)
		{
			Link link = new Link();
			link.setType("text/css");
			link.setRel("stylesheet");
			link.setHref(FileUtils.getWayTo(targetFile.getParentFile(), cssFile));
			doc.head.appendChild(link);
		}
		if (menuBar)
			doc.body.appendChild(navGenerator(targetFile.getParentFile(), root));
		return doc;
	}

	public static void writeDocument(Document document, File html)
	{
		try (FileOutputStream fos = new FileOutputStream(html); OutputStreamWriter osw = new OutputStreamWriter(fos, Charset.forName("UTF-8")))
		{
			osw.write(document.write());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Properties loadProperties(File xml)
	{
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(xml))
		{
			props.loadFromXML(fis);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return props;
	}
}
