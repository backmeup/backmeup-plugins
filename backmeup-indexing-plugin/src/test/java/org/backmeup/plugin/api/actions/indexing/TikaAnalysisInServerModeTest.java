package org.backmeup.plugin.api.actions.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TikaAnalysisInServerModeTest {

    private static TikaServerStub tikaServer;
    DataObject pdf1 = getTestDataObjects("creative-commons.pdf");
    DataObject pdf2 = getTestDataObjects("tika_analyser.pdf");
    DataObject txt1 = getTestDataObjects("tika_analyser.txt");
    DataObject jpg1 = getTestDataObjects("creative-commons.jpg");
    DataObject png1 = getTestDataObjects("creative-commons.png");

    @BeforeClass
    public static void beforeClass() {
        tikaServer = new TikaServerStub();
    }

    @AfterClass
    public static void afterClass() {
        //TODO stop tika server
    }

    @Test
    public void testCallTikaIsAlive() {
        assertTrue(TikaAnalysisInServerModeTest.tikaServer.isTikaAlive());
    }

    @Test
    public void testContentTypeExtraction() throws StorageException {

        try {
            String contentType = tikaServer.detectContentType(this.pdf1);
            assertEquals("application/pdf", contentType);

            contentType = tikaServer.detectContentType(this.pdf2);
            assertEquals("text/plain", contentType);

            contentType = tikaServer.detectContentType(this.png1);
            assertEquals("image/png", contentType);

            contentType = tikaServer.detectContentType(this.jpg1);
            assertEquals("image/jpeg", contentType);

            contentType = tikaServer.detectContentType(this.txt1);
            assertEquals("text/plain", contentType);

        } catch (IOException e) {
            assertTrue(e.toString(), false);
        }
    }

    @Test
    public void testFullTextExtraction() {

        try {
            String fullText = tikaServer.extractFullText(this.pdf1);
            assertTrue(fullText.contains("Crawford starts with her own bookshelf, pulling letters"));

            //in this case content type is recognised as text/plain and fulltext from binary
            fullText = tikaServer.extractFullText(this.pdf2);
            assertTrue(fullText.contains("Ã0ï¿½Â¼Ã»+|Â´Â¥ÃxÃÂ»Ã¶Å¡#ï¿½!UBÂ¢Ã"));

            try {
                //we should get status code 500 for png
                fullText = tikaServer.extractFullText(this.png1);
                assertFalse("FullText extraction from png not possible", true);
            } catch (IOException e) {
                assertTrue(e.toString().contains("received status code 500"));
            }

            try {
                //we should get status code 500 for jpeg
                fullText = tikaServer.extractFullText(this.jpg1);
                assertFalse("FullText extraction from jpeg not possible", true);
            } catch (IOException e) {
                assertTrue(e.toString().contains("received status code 500"));
            }

            fullText = tikaServer.extractFullText(this.txt1);
            assertTrue(fullText.contains("hallo mihai und peter"));

        } catch (IOException e) {
            assertTrue(e.toString(), false);
        }
    }

    @Test
    public void testMetadataExtraction() {

        try {
            Map<String, String> meta = tikaServer.extractMetaData(this.pdf1);
            assertTrue(meta.containsKey("pdf:PDFVersion"));
            assertEquals("1.4", meta.get("pdf:PDFVersion"));
            assertEquals("Adobe PDF Library 7.0", meta.get("producer"));
            assertEquals("2007-03-02T21:50:25Z", meta.get("meta:creation-date"));

            meta = tikaServer.extractMetaData(this.pdf2);
            assertTrue(meta.containsKey("X-Parsed-By"));
            assertTrue(meta.get("X-Parsed-By").contains("org.apache.tika.parser.txt.TXTParser"));
            assertEquals("windows-1252", meta.get("Content-Encoding"));

        } catch (IOException e) {
            assertTrue(e.toString(), false);
        }

        try {
            //we should get status code 500 for png
            Map<String, String> meta = tikaServer.extractMetaData(this.png1);
            assertFalse("Metadata extraction from png not possible", true);
        } catch (IOException e) {
            assertTrue(e.toString().contains("received status code 500"));
        }

        try {
            //we should get status code 500 for png
            Map<String, String> meta = tikaServer.extractMetaData(this.jpg1);
            assertFalse("Metadata extraction from jpg not possible", true);
        } catch (IOException e) {
            assertTrue(e.toString().contains("received status code 500"));
        }
    }

    @Test
    public void testFullTextNegativeCallExamples() {

        String fullText;
        try {
            fullText = tikaServer.extractFullText(this.pdf1, "application/bogus");
            assertTrue(false);
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            fullText = tikaServer.extractFullText(this.pdf1, null);
            assertTrue(false);
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    public void testStartServer() {
        assertFalse(tikaServer.isTikaAlive());

        assertTrue(tikaServer.isTikaAlive());

        assertFalse(tikaServer.isTikaAlive());
    }

    /**
     * Small helper utility to get selected testdata in form ob a DataObject handle
     * 
     * @param filename
     * @return
     */
    private DataObject getTestDataObjects(final String filename) {
        DataObject dob = new DataObject() {
            @Override
            public byte[] getBytes() throws IOException {
                //return IOUtils.toByteArray(new FileReader("src/test/resources/" + filename));
                Path path = Paths.get("src/test/resources/" + filename);
                return Files.readAllBytes(path);
            }

            @Override
            public long getLength() {
                return 21;
            }

            @Override
            public String getPath() {
                return "/" + filename;
            }

            @Override
            public MetainfoContainer getMetainfo() {
                return null;
            }

            @Override
            public void setMetainfo(MetainfoContainer meta) {
            }
        };
        return dob;
    }

}
