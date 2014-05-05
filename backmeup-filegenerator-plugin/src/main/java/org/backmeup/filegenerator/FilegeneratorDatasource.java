package org.backmeup.filegenerator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.backmeup.filegenerator.generator.Generator;
import org.backmeup.filegenerator.generator.impl.BinaryGenerator;
import org.backmeup.filegenerator.generator.impl.ImageGenerator;
import org.backmeup.filegenerator.generator.impl.PdfGenerator;
import org.backmeup.filegenerator.generator.impl.TextGenerator;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class FilegeneratorDatasource implements Datasource {
	private static final String PDF_GENERATOR = "pdf";
	private static final String TEXT_GENERATOR = "text";
	private static final String IMAGE_GENERATOR = "image";
	private static final String BINARY_GENERATOR = "binary";

	@Override
	public void downloadAll(Properties accessData, List<String> options,
			Storage storage, Progressable progressor)
			throws DatasourceException, StorageException {
		final Random random = new Random();
		final ArrayList<Generator> generators = new ArrayList<Generator>();

		String fileTypes = "text,pdf,binary,image";
		// removes all whitespace and non visible characters
		fileTypes.replaceAll("\\s", "");
		String[] fileTypesArray = fileTypes.split(",");

		for (String fileType : fileTypesArray) {
			if (fileType.equals(TEXT_GENERATOR)) {
				int txtAmountParagraphs = 100;
				generators.add(new TextGenerator(txtAmountParagraphs));
			} else if (fileType.equals(IMAGE_GENERATOR)) {
				int imgSize = 1024;
				generators.add(new ImageGenerator(imgSize, imgSize, random));
			} else if (fileType.equals(PDF_GENERATOR)) {
				String pdfTitle = "Lorem ipsum";
				int pdfAmountParagraphs = 100;
				String pdfText = new TextGenerator().getParagraphs(pdfAmountParagraphs);
				generators.add(new PdfGenerator(pdfTitle, pdfText));
			} else if (fileType.equals(BINARY_GENERATOR)) {
				int binSize = 1024;
				generators.add(new BinaryGenerator(binSize, random));
			} else {
				throw new RuntimeException("File generator type is not supported: " + fileType);
			}
		}

		int maxFiles = 1000;
		int currentFiles = 0;
		int currentGeneratorIndex = 0;
		int noOfGenerators = generators.size();
		String filenamePrefix = "file";
		String filePath = "/";
		
//		long currentTime = new Date().getTime();
//		long endTime;
		
		while (currentFiles < maxFiles) {
			// Select generator
			currentGeneratorIndex = currentFiles % noOfGenerators;
			Generator generator = generators.get(currentGeneratorIndex);
			
			// Assemble file path
			String filename = filenamePrefix + currentFiles;
			
			// Create metainfo based on the selected generator
			MetainfoContainer cont = new MetainfoContainer();
			if(generator instanceof TextGenerator) {
				String filenameExtension = ".txt";
				filename = filePath + filename + filenameExtension;
				cont.addMetainfo(createMetainfo(currentFiles + "", "text/plain", filename));
				
			} else if (generator instanceof ImageGenerator) {
				String filenameExtension = ".jpg";
				filename = filePath + filename + filenameExtension;
				cont = new MetainfoContainer();
				cont.addMetainfo(createMetainfo(currentFiles + "", "image/jpeg", filename));
			} else if (generator instanceof PdfGenerator) {
				String filenameExtension = ".pdf";
				filename = filePath + filename + filenameExtension;
				cont = new MetainfoContainer();
				cont.addMetainfo(createMetainfo(currentFiles + "", "application/pdf", filename));
			} else if (generator instanceof BinaryGenerator) {
				String filenameExtension = ".bin";
				filename = filePath + filename + filenameExtension;
				cont = new MetainfoContainer();
				cont.addMetainfo(createMetainfo(currentFiles + "", "application/octet-stream", filename));
			} else {
				throw new RuntimeException("File generator type is not supported");
			}
			
			InputStream is = generator.generate();
			storage.addFile(is, filename, cont);
			
			progressor.progress(String.format("Generated file %s ...", filename));
			
			currentFiles++;
		}
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
