package org.backmeup.plugin.api.actions.indexing;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.backmeup.index.api.IndexClient;
import org.backmeup.index.model.FileInfo;
import org.backmeup.index.model.FileItem;
import org.backmeup.index.model.IndexDocument;
import org.backmeup.index.model.SearchResultAccumulator;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Assert;
import org.junit.Test;

public class IndexActionTikaAnlyserTest {

    private IndexDocument actualDocument;

    @Test
    //@Ignore("add tika dependencies for this test")
    public void shouldExtractFullTextOfPDF() throws ActionException {
        FakeIndexClient fakeClient = new FakeIndexClient();
        IndexAction action = new IndexAction(fakeClient);

        Storage pdfStorage = new FakeFileStorage();
        Progressable progressor = new Progressable() {
            @Override
            public void progress(String message) {
                System.out.println(message);
            }
        };
        BackupJobDTO job = new BackupJobDTO();
        UserDTO user = new UserDTO() {
            @Override
            public Long getUserId() {
                return null;
            };
        };

        job.setUser(user);
        PluginProfileDTO profile = new PluginProfileDTO();
        profile.setProfileType(PluginType.Sink);
        //profile.set();

        job.setSink(profile);
        // TODO fill properties
        action.doAction(new HashMap<String, String>(), new HashMap<String, String>(), new ArrayList<String>(),
                pdfStorage, job, progressor);

        // fakeClient got a document, must contain full text
        assertEquals("1.4", this.actualDocument.getFields().get("pdf:PDFVersion"));
    }

    class FakeIndexClient implements IndexClient {

        @Override
        public void index(IndexDocument document) throws IOException {
            IndexActionTikaAnlyserTest.this.actualDocument = document;
        }

        // here for tests...

        @Override
        public SearchResultAccumulator queryBackup(String query, String filterBySource, String filterByType,
                String filterByJob, String username) {
            Assert.fail("do not call");
            return null;
        }

        @Override
        public Set<FileItem> searchAllFileItemsForJob(Long jobId) {
            Assert.fail("do not call");
            return null;
        }

        @Override
        public FileInfo getFileInfoForFile(String fileId) {
            Assert.fail("do not call");
            return null;
        }

        @Override
        public String getThumbnailPathForFile(String fileId) {
            Assert.fail("do not call");
            return null;
        }

        @Override
        public void deleteRecordsForUser() {
            Assert.fail("do not call");
        }

        @Override
        public void deleteRecordsForJobAndTimestamp(Long jobId, Date timestamp) {
            Assert.fail("do not call");
        }

        @Override
        public void close() {
        }

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
                    return "abc/creative-commons.pdf";
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
