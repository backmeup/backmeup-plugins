package org.backmeup.filegenerator.generator.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.backmeup.filegenerator.FilegeneratorDescriptor;
import org.backmeup.filegenerator.generator.Generator;
import org.backmeup.model.exceptions.PluginException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfGenerator implements Generator {
	private final String title;
	private final String body;
	
	Font titleFont;
	Font bodyFont;
	
	Document document;
	
	public PdfGenerator() {
		this("Lorem Ipsum", new TextGenerator().getParagraphs(1));
	}
	
	public PdfGenerator(String title, String body) {
		titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
		bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
		
		this.title = title;
		this.body = body;
	}
	
	@Override
	public InputStream generate() {
		try {
			ByteArrayOutputStream osPdf = new ByteArrayOutputStream();
			
			this.document = new Document();
			PdfWriter.getInstance(document, osPdf);

			this.document.open();
			document.addTitle(this.title);
			document.addKeywords("Backmeup, backup");
			document.addAuthor("backmeup");
			document.addCreator("backmeup-filegenerator-plugin");

			Paragraph content = new Paragraph();
			content.add(new Paragraph(" "));
			content.add(new Paragraph(this.title, titleFont));
			content.add(new Paragraph(" "));
			content.add(new Paragraph(this.body, bodyFont));

			document.add(content);
			document.close();
			
			return new ByteArrayInputStream(osPdf.toByteArray());
		} catch (DocumentException e) {
			throw new PluginException(FilegeneratorDescriptor.FILEGENERATOR_ID, "Failed to generate pdf document", e);
		}
	}
}
