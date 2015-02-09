package org.backmeup.plugin.api.actions.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Before;
import org.junit.Test;

public class TikaAnalysisInServerModeTest {

    private TikaServerStub tikaServer;
    DataObject pdf1 = getTestDataObjects("creative-commons.pdf");
    DataObject pdf2 = getTestDataObjects("tika_analyser.pdf");
    DataObject txt1 = getTestDataObjects("tika_analyser.txt");
    DataObject jpg1 = getTestDataObjects("creative-commons.jpg");
    DataObject png1 = getTestDataObjects("creative-commons.png");

    @Before
    public void before() {
        this.tikaServer = new TikaServerStub();
    }

    @Test
    public void testCallTikaMetadataAnalysis() {
        try {
            this.tikaServer.extractMeta();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testCallTikaIsAlive() {
        assertTrue(this.tikaServer.isTikaAlive());
    }

    @Test
    public void testContentTypeExtraction() throws StorageException {

        try {
            String contentType = this.tikaServer.detectContentType(this.pdf1);
            assertEquals("application/pdf", contentType);

            contentType = this.tikaServer.detectContentType(this.pdf2);
            assertEquals("text/plain", contentType);

            contentType = this.tikaServer.detectContentType(this.png1);
            assertEquals("image/png", contentType);

            contentType = this.tikaServer.detectContentType(this.jpg1);
            assertEquals("image/jpeg", contentType);

            contentType = this.tikaServer.detectContentType(this.txt1);
            assertEquals("text/plain", contentType);

        } catch (IOException e) {
            assertTrue(e.toString(), false);
        }

    }

    public void testStartServer() {
        assertFalse(this.tikaServer.isTikaAlive());

        assertTrue(this.tikaServer.isTikaAlive());

        assertFalse(this.tikaServer.isTikaAlive());
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
                return IOUtils.toByteArray(new FileReader("src/test/resources/" + filename));
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
