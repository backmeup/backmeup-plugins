package org.backmeup.filegenerator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import de.svenjacobs.loremipsum.LoremIpsum;

public class FilegeneratorDatasource implements Datasource {
	@Override
	public void downloadAll(Properties accessData, List<String> options, Storage storage, Progressable progressor)
			throws DatasourceException, StorageException {
		
		
		// Plain text ---------------------------------------------------------
		
		MetainfoContainer cont = new MetainfoContainer();
		cont.addMetainfo(createMetainfo("1", "text/plain", "/plain.txt"));
		LoremIpsum loremIpsum = new LoremIpsum();
		InputStream is = stringToStream(loremIpsum.getParagraphs(100));
		storage.addFile(is, "/plain.txt", cont);
		// ====================================================================
		
		// Image --------------------------------------------------------------
		cont = new MetainfoContainer();
		cont.addMetainfo(createMetainfo("2", "image/jpeg", "/img.jpg"));
		BufferedImage image = ImageCreator.generate(1024, 1024, 1);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "jpg", os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		is = new ByteArrayInputStream(os.toByteArray());
		storage.addFile(is, "/img.jpg", cont);
		// ====================================================================
		
		// Pdf ----------------------------------------------------------------
		cont = new MetainfoContainer();
		cont.addMetainfo(createMetainfo("1", "text/pdf", "/text.pdf"));
		Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD);
		
		Document document = new Document();
		document.open();
		document.addTitle("Lorem Ipsum");
	    document.addKeywords("Backmeup, backup, lorem, ipsum");
	    document.addAuthor("backmeup");
	    document.addCreator("backmeup-filegenerator-plugin");
	    
	    Paragraph content = new Paragraph();
	    content.add(new Paragraph(" "));
	    content.add(new Paragraph("Lorem Impsum", catFont));
	    content.add(new Paragraph(" "));
	    content.add(new Paragraph(loremIpsum.getParagraphs(100)));
	    
	    document.close();
	    ByteArrayOutputStream osPdf = new ByteArrayOutputStream();
	    try {
			PdfWriter.getInstance(document, osPdf);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		is =  new ByteArrayInputStream(osPdf.toByteArray());
		storage.addFile(is, "/text.pdf", cont);
		// ====================================================================
	}

	@Override
	public String getStatistics(Properties accesssData) {
		return "statistics are empty";
	}

	@Override
	public List<String> getAvailableOptions(Properties accessData) {
		List<String> options = new ArrayList<String>();
		options.add("option1");
		options.add("option2");
		return options;
	}

	private InputStream stringToStream(String input) {
		try {
			InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
			return is;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Metainfo createMetainfo(String id, String type, String destination) {
		Metainfo info = new Metainfo();
		info.setBackupDate(new Date());
		info.setDestination(destination);
		info.setId(id);
		info.setSource("filegenerator");
		info.setType(type);
		return info;
	}

}
