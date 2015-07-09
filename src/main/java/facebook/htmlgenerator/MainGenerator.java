/**
* @author Richard STöckl
*/

package facebook.htmlgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import com.hp.gagawa.java.Document;
import com.hp.gagawa.java.DocumentType;
import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Div;
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

import facebook.storage.AlbumInfoKeys;
import facebook.storage.Data;
import facebook.storage.EndingFilter;
import facebook.storage.FilePaths;
import facebook.storage.PhotoInfoKeys;
import facebook.storage.ReplaceID;
import facebook.storage.SDO;
import facebook.storage.UserInfoKeys;
import facebook.utils.FileUtils;

public class MainGenerator
{
	private String working_dir = "", output_folder;
	private File work_dir, out_dir;

	public MainGenerator(String workingdir)
	{
		this.working_dir = workingdir;
		this.output_folder = working_dir + "/out";
		work_dir = new File(working_dir);
		if (!work_dir.exists())
			work_dir.mkdirs();
		out_dir = new File(output_folder);
		if (!out_dir.exists())
			out_dir.mkdirs();
	}

	public void genOverview()
	{

		Document index = new Document(DocumentType.HTMLTransitional);
		
		index.body.appendChild(menuGenerator());
		
		Properties userProps = new Properties();
		File target = new File("" + working_dir + SDO.SLASH + FilePaths.USER_FILE);
		File indexTarget = new File("" + out_dir + SDO.SLASH + "index.html");
		try (FileInputStream fis = new FileInputStream(target);)
		{
			userProps.loadFromXML(fis);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initDocumentHeader(index, userProps.getProperty(UserInfoKeys.FIRST_NAME.toString()), indexTarget, null, false);
		Div sideInfos = new Div();
		sideInfos.setCSSClass("sidebar");
		sideInfos.appendChild(wrapInfos(UserInfoKeys.values(), userProps, true));
		index.body.appendChild(sideInfos);
		genAlbums(index);
		try (FileWriter fw = new FileWriter(indexTarget))
		{
			fw.write(index.write());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void genAlbums(Document index)
	{
		// File albumfile = new File("" + OUT_DIR + SDO.SLASH + "albums.html");
		File albumFolder = new File("" + out_dir + SDO.SLASH + FilePaths.ALBUMS_DIRECTORY);
		if (!albumFolder.exists())
			albumFolder.mkdirs();
		Document albums = new Document(DocumentType.HTMLTransitional);
		File target = new File("" + out_dir + SDO.SLASH + "albums.html");
		albums = initDocumentHeader(albums, "Alben", target, null, true);
		Div albumContainer = new Div();
		albumContainer.setCSSClass("picture_container");
		Ul albumlist = new Ul();
		for (File albumfolder : new File("" + work_dir + SDO.SLASH + FilePaths.ALBUMS_DIRECTORY).listFiles())
		{
			Properties albumsProps = new Properties();
			File albumInfoFile = new File("" + work_dir + SDO.SLASH + FilePaths.ALBUM_INFO.toString().replace("" + ReplaceID.ALBUM_ID, albumfolder.getName()));
			try (FileInputStream fis = new FileInputStream(albumInfoFile))
			{
				albumsProps.loadFromXML(fis);
				Div innerItem = new Div();
				Li item = new Li();
				// item.appendText(albumsProps.getProperty(AlbumInfoKeys.LAST_UPDATE.toString()));
				String relativeImg = FileUtils.getWayTo(out_dir, albumfolder) + SDO.SLASH + albumsProps.getProperty(AlbumInfoKeys.COVER_PHOTO_ID.toString()) + ".jpg";
				A albumLink = new A();
				Img cover = new Img("baum", relativeImg);
				innerItem.setCSSClass("album_picture");
				innerItem.appendChild(cover);
				albumLink.appendChild(innerItem);
				albumLink.setHref(FileUtils.getWayTo(out_dir, new File("" + out_dir + SDO.SLASH + FilePaths.ALBUMS_DIRECTORY + SDO.SLASH + albumfolder.getName() + ".html")));
				// innerItem.appendChild(albumLink);
				String albumName = albumsProps.getProperty(AlbumInfoKeys.NAME.toString());
				String desc = albumsProps.getProperty(AlbumInfoKeys.DESCRIPTION.toString());
				P textBelow = new P();
				if (albumName != null)
					textBelow.appendText(albumName);
				if (desc != null)
					textBelow.appendText("<br/>" + desc);
				innerItem.appendChild(textBelow);
				item.appendChild(albumLink);
				albumlist.appendChild(item);
				genAlbum(albumFolder, albumfolder.getName());
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		albumContainer.appendChild(albumlist);
		albums.body.appendChild(albumContainer);
		try (FileWriter fw = new FileWriter(target))
		{
			fw.write(albums.write());
		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	private void genAlbum(File albumsFolder, String albumID)
	{
		// TODO: Seite erstellen, wo pro Album die einzelnen Bilder verlinkt
		// werden
		Document albumFile = new Document(DocumentType.HTMLTransitional);
		File photoHtmlContainer = new File("" + albumsFolder + SDO.SLASH + albumID);
		if (!photoHtmlContainer.exists())
			photoHtmlContainer.mkdirs();
		Properties albumXml = new Properties();
		File target = new File("" + albumsFolder + SDO.SLASH + albumID + ".html");
		try (FileInputStream fis = new FileInputStream("" + work_dir + SDO.SLASH + FilePaths.ALBUM_INFO.toString().replace(ReplaceID.ALBUM_ID.toString(), albumID)); FileWriter fw = new FileWriter(target))
		{
			albumXml.loadFromXML(fis);
			File originalAlbumFolder = new File("" + work_dir + SDO.SLASH + FilePaths.ALBUM_DIRECTORY.toString().replace(ReplaceID.ALBUM_ID.toString(), albumID));
			File cover = new File("" + originalAlbumFolder + SDO.SLASH + albumXml.getProperty(AlbumInfoKeys.COVER_PHOTO_ID.toString()) + ".jpg");
			albumFile = initDocumentHeader(albumFile, albumXml.getProperty(AlbumInfoKeys.NAME.toString(), "Album"), target, cover, true);
			Div photoContainer = new Div();
			photoContainer.setCSSClass("picture_container");
			Ul photoList = new Ul();
			for (File photoXml : originalAlbumFolder.listFiles(new EndingFilter("xml")))
			{
				if (!photoXml.getName().equalsIgnoreCase("albuminfo.xml"))
					// System.out.println(photoXml);
					try (FileInputStream fisPhotoXml = new FileInputStream(photoXml))
					{
						Properties photoProps = new Properties();
						photoProps.loadFromXML(fisPhotoXml);
						// System.out.println(photoProps);
						Li photoItem = new Li();
						A photoLink = new A();
						photoLink.setHref(FileUtils.getWayTo(albumsFolder, new File("" + photoHtmlContainer + SDO.SLASH + photoProps.getProperty(PhotoInfoKeys.ID.toString()))) + ".html");
						Div innerItem = new Div().setCSSClass("album_picture");
						String relativePhoto = FileUtils.getWayTo(albumsFolder, new File("" + originalAlbumFolder + SDO.SLASH + photoProps.getProperty(PhotoInfoKeys.FILE.toString())));
						innerItem.appendChild(new Img("Photo", relativePhoto));
						innerItem.appendChild(new P().appendText("Likes: " + photoProps.getProperty(PhotoInfoKeys.LIKES.toString())));
						photoLink.appendChild(innerItem);
						photoItem.appendChild(photoLink);
						photoList.appendChild(photoItem);

						genPhotoFile(photoProps, photoHtmlContainer, originalAlbumFolder);
					} catch (IOException e)
					{
						e.printStackTrace();
					}
			}

			Div sideInfos = new Div();
			sideInfos.setCSSClass("sidebar");
			sideInfos.appendChild(wrapInfos(AlbumInfoKeys.values(), albumXml, true));

			photoContainer.appendChild(photoList);
			albumFile.body.appendChild(sideInfos);
			albumFile.body.appendChild(photoContainer);
			fw.write(albumFile.write());

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Table wrapInfos(Data[] aik, Properties albumsProps, boolean skipinvalid)
	{
		Table table = new Table();
		for (Data key : aik)
		{
			String value = albumsProps.getProperty(key.toString());
			if ((value != null && !value.equals("")) || !skipinvalid)
			{
				if (value == null)
					value = "keine Infos vorhanden";
				if (key.isDate())
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
				if (key.isLink())
				{
					A link = new A();
					link.appendText(key.getLabel());
					link.setHref(value);
					value = link.write();
				}
				Tr row = new Tr();
				row.appendChild(new Td().appendText(key.getLabel()));
				row.appendChild(new Td().appendText(value));
				table.appendChild(row);
			}
		}
		return table;
	}

	/*
	 * public static Date getDateFromString() { Calendar. }
	 */

	public Node menuGenerator()
	{
		Div menubar = new Div();
		Ul items = new Ul();
		Li albumListedLink = new Li();
		A albumLink = new A();
		albumLink.setHref(FileUtils.getWayTo(out_dir, new File("" + out_dir + SDO.SLASH + "albums.html")));
		albumListedLink.appendChild(albumLink);
		albumLink.appendText("Alben");
		items.appendChild(albumListedLink);
		menubar.appendChild(items);
		return menubar;
	}

	/*
	 * public Link getMainCSS(File dir) { Link link = new Link();
	 * link.setType("text/css"); link.setRel("stylesheet");
	 * link.setHref(FileUtils.getWayTo(dir, new File("" + OUT_DIR + SDO.SLASH +
	 * "main.css"))); return link; }
	 */

	public void genPhotoFile(Properties photoProps, File dir, File albumDir)
	{
		File target = new File("" + dir + SDO.SLASH + photoProps.getProperty(PhotoInfoKeys.ID.toString()) + ".html");
		File icon = new File("" + albumDir + SDO.SLASH + photoProps.getProperty(PhotoInfoKeys.FILE.toString()));
		Document photoDoc = new Document(DocumentType.HTMLTransitional);
		photoDoc = initDocumentHeader(photoDoc, photoProps.getProperty(PhotoInfoKeys.ID.toString(), "Foto"), target, icon, true);
		String relativeImg = FileUtils.getWayTo(dir, icon);
		Img picture = new Img("Photo", relativeImg);
		Div sideInfos = new Div();
		sideInfos.setCSSClass("sidebar");
		sideInfos.appendChild(wrapInfos(PhotoInfoKeys.values(), photoProps, true));
		photoDoc.body.appendChild(sideInfos);
		photoDoc.body.appendChild(picture);
		P likes = new P();
		likes.appendText("Likes: " + photoProps.getProperty(PhotoInfoKeys.LIKES.toString()));
		photoDoc.body.appendChild(likes);
		if (Integer.parseInt(photoProps.getProperty(PhotoInfoKeys.LIKES.toString())) > 0)
		{
			P likePersons = new P();
			likePersons.appendText("Geliked von: " + photoProps.getProperty(PhotoInfoKeys.LIKES_FROM_PEOPLE.toString()));
			photoDoc.body.appendChild(likePersons);
		}
		try (FileWriter fw = new FileWriter(target))
		{
			fw.write(photoDoc.write());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Node navGenerator(File htmlDir)
	{
		Div container = new Div();
		container.setCSSClass("navContainer");
		Ul menuList = new Ul();
		menuList = appendItem(menuList, htmlDir, new File("" + out_dir + SDO.SLASH + "index.html"), "Home");
		menuList = appendItem(menuList, htmlDir, new File("" + out_dir + SDO.SLASH + "albums.html"), "Alben");
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

	private Document initDocumentHeader(Document doc, String title, File targetFile, File icon, boolean menuBar, File... cssFiles)
	{
		System.out.println(targetFile);
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
		instantcssFiles.add(new File("" + out_dir + SDO.SLASH + "menu.css"));
		instantcssFiles.add(new File("" + out_dir + SDO.SLASH + "main.css"));
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
			doc.body.appendChild(navGenerator(targetFile.getParentFile()));
		return doc;
	}
}
