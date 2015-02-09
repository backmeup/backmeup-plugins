package org.backmeup.plugin.api.actions.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TikaAnalysisInServerModeTest {

    private TikaServerStub tikaServer;
    DataObject pdf1 = getTestObject("creative-commons.pdf");
    DataObject pdf2 = getTestObject("tika_analyser.pdf");
    DataObject txt1 = getTestObject("tika_analyser.txt");
    DataObject jpg1 = getTestObject("creative-commons.jpg");
    DataObject png1 = getTestObject("creative-commons.png");

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

    private DataObject getTestObject(final String filename) {
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

    public class FakeFileStorage implements Storage {

        @Override
        public Iterator<DataObject> getDataObjects() throws StorageException {
            DataObject pdfDob = new DataObject() {

                @Override
                public byte[] getBytes() throws IOException {
                    return IOUtils.toByteArray(new FileReader("src/test/resources/creative-commons.pdf"));
                }

                @Override
                public long getLength() {
                    return 21;
                }

                @Override
                public String getPath() {
                    return "/creative-commons.pdf";
                }

                @Override
                public MetainfoContainer getMetainfo() {
                    return null;
                }

                @Override
                public void setMetainfo(MetainfoContainer meta) {
                }
            };
            return Arrays.asList(pdfDob).iterator();
        }

        // here for tests...

        @Override
        public void open(String path) throws StorageException {
            Assert.fail("do not call");
        }

        @Override
        public void close() throws StorageException {
            Assert.fail("do not call");
        }

        @Override
        public void delete() throws StorageException {
            Assert.fail("do not call");
        }

        @Override
        public int getDataObjectCount() throws StorageException {
            Assert.fail("do not call");
            return 0;
        }

        @Override
        public long getDataObjectSize() throws StorageException {
            Assert.fail("do not call");
            return 0;
        }

        @Override
        public boolean existsPath(String path) throws StorageException {
            Assert.fail("do not call");
            return false;
        }

        @Override
        public void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException {
            Assert.fail("do not call");
        }

        @Override
        public void removeFile(String path) throws StorageException {
            Assert.fail("do not call");
        }

        @Override
        public void removeDir(String path) throws StorageException {
            Assert.fail("do not call");
        }

        @Override
        public void move(String fromPath, String toPath) throws StorageException {
            Assert.fail("do not call");
        }

    }

}
