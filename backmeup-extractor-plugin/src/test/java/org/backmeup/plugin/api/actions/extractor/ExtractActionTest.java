package org.backmeup.plugin.api.actions.extractor;

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
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.dto.UserDTO;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.actions.extractor.ExtractorAction;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.DummyStorage;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class ExtractActionTest {
	
	@Before
    public void setup() throws ActionException {
        System.out.println("Setting up test index...");
	}

    @Test
    public void shouldExtractContentTypeOfPDF() throws ActionException {

    	ExtractorAction action = new ExtractorAction();
    	
        Storage pdfStorage = new FakeFileStorage();
        Progressable progressor = new Progressable() {
            @Override
            public void progress(String message) {
                System.out.println(message);
            }
        };
        
        BackupJobExecutionDTO job = new BackupJobExecutionDTO();
        
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
        action.doAction(new HashMap<String, String>(), new HashMap<String, String>(), new ArrayList<String>(), pdfStorage, job, progressor);

        // fakeClient got a document, must contain content-type
        //assertEquals("application/pdf", this.actualDocument.getFields().get("Content-Type"));
    }

    public class FakeFileStorage implements Storage {

        @Override
        public Iterator<DataObject> getDataObjects() throws StorageException {
            DataObject pdfDob = new DataObject() {

                @Override
                public byte[] getBytes() throws IOException {
                    return IOUtils.toByteArray(new FileReader("src/test/resources/sample_vcard.vcf"));
                }

                @Override
                public long getLength() {
                    return 21;
                }

                @Override
                public String getPath() {
                    return "src/test/resources/sample_vcard.vcf";
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
