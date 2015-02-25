package org.backmeup.plugin.api.actions.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TikaAnalysisInServerModeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TikaServerStub tika = new TikaServerStub();
    private static TikaServerStartupHandler h = new TikaServerStartupHandler();

    DataObject pdf1 = getTestDataObjects("creative-commons.pdf");
    DataObject pdf2 = getTestDataObjects("tika_analyser.pdf");
    DataObject txt1 = getTestDataObjects("tika_analyser.txt");
    DataObject jpg1 = getTestDataObjects("creative-commons.jpg");
    DataObject png1 = getTestDataObjects("creative-commons.png");
    DataObject html1 = getTestDataObjects("tika_analyser.html");
    DataObject jpg2 = getTestDataObjects("tika_analyser_exif.jpg");
    DataObject docx1 = getTestDataObjects("tika_analyser.docx");

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        h.startTikaServer();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void afterClass() {
        h.stopTikaServer();
    }

    @Test
    public void testCallTikaIsAlive() {
        assertTrue(this.tika.isTikaAlive());
    }

    @Test
    public void testContentTypeExtraction() throws IOException {

        String contentType = this.tika.detectContentType(this.pdf1);
        assertEquals("application/pdf", contentType);

        contentType = this.tika.detectContentType(this.pdf2);
        assertEquals("text/plain", contentType);

        contentType = this.tika.detectContentType(this.png1);
        assertEquals("image/png", contentType);

        contentType = this.tika.detectContentType(this.jpg1);
        assertEquals("image/jpeg", contentType);

        contentType = this.tika.detectContentType(this.jpg2);
        assertEquals("image/jpeg", contentType);

        contentType = this.tika.detectContentType(this.html1);
        assertEquals("text/html", contentType);

        contentType = this.tika.detectContentType(this.docx1);
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", contentType);

        contentType = this.tika.detectContentType(this.txt1);
        assertEquals("text/plain", contentType);
    }

    @Test
    public void testFullTextExtractionDocuments() throws IOException {

        String fullText = this.tika.extractFullText(this.pdf1);
        assertTrue(fullText.contains("Crawford starts with her own bookshelf, pulling letters"));
        assertFalse("don't want metadata header in the fulltext", fullText.contains("Content-Disposition"));
        assertFalse("don't want metadata header in the fulltext", fullText.contains("Content-Type"));
        assertFalse("don't want metadata header in the fulltext", fullText.contains("Content-Transfer-Encoding"));

        //in this case content type is recognised as text/plain and fulltext from binary
        fullText = this.tika.extractFullText(this.pdf2);
        assertTrue(fullText.contains("´�dr?�»n�ÙýzÈ�áa�"));

        fullText = this.tika.extractFullText(this.txt1);
        assertTrue(fullText.contains("hallo mihai und peter"));
        assertFalse("don't want metadata header in the fulltext", fullText.contains("Content-Disposition"));
        assertFalse("don't want metadata header in the fulltext", fullText.contains("Content-Type"));
        assertFalse("don't want metadata header in the fulltext", fullText.contains("Content-Transfer-Encoding"));

        fullText = this.tika.extractFullText(this.html1);
        assertTrue(fullText.contains("translation with @Bing Translator so you can read Tweets in multiple"));
    }

    @Test
    public void testFullTextExtractionImagesPNG() throws IOException {
        //fulltext extraction for png not possible expecting status code 500
        this.thrown.expectMessage("received status code 500");
        this.thrown.expect(IOException.class); //expecting IOException to be thrown
        this.tika.extractFullText(this.png1);
    }

    @Test
    public void testFullTextExtractionImagesJPEG() throws IOException {
        //fulltext extraction for jpeg not possible expecting status code 500
        this.thrown.expect(IOException.class); //expecting IOException to be thrown
        this.thrown.expectMessage("received status code 500");
        this.tika.extractFullText(this.jpg1);
    }

    @Test
    @Ignore("//TODO AL refs #193")
    public void testFullTextExtractionDocumentsDOCX() throws IOException {
        //TODO doxc fulltext extraction not working
        String fullText = this.tika.extractFullText(this.docx1);
        System.out.println(fullText);
        assertTrue(fullText.contains("Download the latest stable source from"));
    }

    @Test
    public void testMetadataExtraction() throws IOException {

        Map<String, String> meta = this.tika.extractMetaData(this.pdf1);
        assertTrue(meta.containsKey("pdf:PDFVersion"));
        assertEquals("1.4", meta.get("pdf:PDFVersion"));
        assertEquals("Adobe PDF Library 7.0", meta.get("producer"));
        assertEquals("2007-03-02T21:50:25Z", meta.get("meta:creation-date"));

        meta = this.tika.extractMetaData(this.pdf2);
        assertTrue(meta.containsKey("X-Parsed-By"));
        assertTrue(meta.get("X-Parsed-By").contains("org.apache.tika.parser.txt.TXTParser"));
        assertEquals("windows-1252", meta.get("Content-Encoding"));

        meta = this.tika.extractMetaData(this.html1);
        assertNotNull(meta.get("title"));
        assertEquals("Your e-mail backup", meta.get("title"));
    }

    @Test
    public void testMetadataExtractionImagesPNG() throws IOException {
        //metadata extraction for png not possible expecting status code 500
        this.thrown.expectMessage("received status code 500");
        this.thrown.expect(IOException.class); //expecting IOException to be thrown
        this.tika.extractMetaData(this.png1);
    }

    @Test
    @Ignore("TODO AL refs #193")
    public void testMetadataExtractionImagesJPEG() throws IOException {
        //TODO images with and without exif information work on command line but not via JAX-RS calls
        //curl -H "Accept: text/csv" -T tika_analyser_exif.jpg http://localhost:9998/meta
        Map<String, String> meta = this.tika.extractMetaData(this.jpg1);
        assertTrue(meta.containsKey("X-Parsed-By"));
        assertTrue(meta.get("X-Parsed-By").contains("org.apache.tika.parser.jpeg.JpegParser"));
    }

    @Test
    @Ignore("TODO AL refs #193")
    public void testMetadataExtractionDocumentsDOCX() throws IOException {
        //TODO doxc and images with exif information work via curl but not via JAX-RS calls
        //curl -H "Accept: text/csv" -T tika_analyser.docx http://localhost:9998/meta
        Map<String, String> meta = this.tika.extractMetaData(this.docx1);
        assertTrue(meta.get("X-Parsed-By").contains("org.apache.tika.parser.microsoft.ooxml.OOXMLParser"));
        assertTrue(meta.get("meta:character-count-with-spaces").equals("9162"));
    }

    @Test
    public void testFullTextNegativeCallExample1() throws IOException {
        this.thrown.expect(IOException.class); //expecting IOException to be thrown
        this.thrown.expectMessage("received status code 415");
        this.tika.extractFullText(this.pdf1, "application/bogus");
    }

    @Test
    public void testFullTextNegativeCallExample2() throws IOException {
        this.thrown.expect(IOException.class); //expecting IOException to be thrown
        this.thrown.expectMessage("received status code 415");
        this.tika.extractFullText(this.pdf1, null);
    }

    /**
     * Small helper utility to get selected testdata in form of a DataObject handle
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
