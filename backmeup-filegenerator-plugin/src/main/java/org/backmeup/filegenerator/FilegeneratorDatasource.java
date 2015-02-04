package org.backmeup.filegenerator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.backmeup.filegenerator.constants.Constants;
import org.backmeup.filegenerator.generator.Generator;
import org.backmeup.filegenerator.generator.impl.BinaryGenerator;
import org.backmeup.filegenerator.generator.impl.ImageGenerator;
import org.backmeup.filegenerator.generator.impl.PdfGenerator;
import org.backmeup.filegenerator.generator.impl.TextGenerator;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class FilegeneratorDatasource implements Datasource {
    private static final int DEFAULT_MAX_FILES = Integer.parseInt(Constants.PROP_GENERATOR_FILES_DEFAULT);

    @Override
    public void downloadAll(Properties authData, Properties properties, List<String> options, Storage storage,
            Progressable progressor) throws StorageException {
        final Random random = new Random();
        final ArrayList<Generator> generators = new ArrayList<>();

        if (properties.getProperty(Constants.PROP_TEXT).equals("true")) {
            String amount = properties.getProperty(Constants.PROP_TEXT_PARAGRAPHS);
            try {
                //note: amount might be null or "" or a valid integer
                int txtAmountParagraphs = Integer.parseInt(amount);
                generators.add(new TextGenerator(txtAmountParagraphs));
            } catch (NumberFormatException e) {
                generators.add(new TextGenerator());
            }
        }

        if (properties.getProperty(Constants.PROP_IMAGE).equals("true")) {
            String size = properties.getProperty(Constants.PROP_IMAGE_SIZE);
            try {
                int imgSize = Integer.parseInt(size);
                generators.add(new ImageGenerator(imgSize, imgSize, random));
            } catch (NumberFormatException e) {
                generators.add(new ImageGenerator());
            }
        }

        if (properties.getProperty(Constants.PROP_PDF).equals("true")) {
            String amount = properties.getProperty(Constants.PROP_PDF_PARAGRAPHS);
            try {
                int pdfAmountParagraphs = Integer.parseInt(amount);
                String pdfText = new TextGenerator().getParagraphs(pdfAmountParagraphs);
                String pdfTitle = "Lorem ipsum";
                generators.add(new PdfGenerator(pdfTitle, pdfText));
            } catch (NumberFormatException e) {
                generators.add(new PdfGenerator());
            }
        }

        if (properties.getProperty(Constants.PROP_BINARY).equals("true")) {
            String size = properties.getProperty(Constants.PROP_BINARY_SIZE);
            try {
                int binSize = Integer.parseInt(size);
                generators.add(new BinaryGenerator(binSize, random));
            } catch (NumberFormatException e) {
                generators.add(new BinaryGenerator());
            }
        }

        int maxFiles;
        int currentFiles = 0;
        int currentGeneratorIndex = 0;
        int noOfGenerators = generators.size();
        String filenamePrefix = "file";
        String filePath = "/";

        //		long currentTime = new Date().getTime();
        //		long endTime;

        String files = properties.getProperty(Constants.PROP_GENERATOR_FILES);
        try {
            maxFiles = Integer.parseInt(files);
        } catch (NumberFormatException e) {
            maxFiles = DEFAULT_MAX_FILES;
        }

        while (currentFiles < maxFiles) {
            // Select generator
            currentGeneratorIndex = currentFiles % noOfGenerators;
            Generator generator = generators.get(currentGeneratorIndex);

            // Assemble file path
            String filename = filenamePrefix + currentFiles;

            // Create metainfo based on the selected generator
            MetainfoContainer cont = new MetainfoContainer();
            if (generator instanceof TextGenerator) {
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
