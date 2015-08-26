package org.backmeup.plugin.api.actions.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.backmeup.model.BackupJob;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAO;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DummyStorage;
import org.backmeup.plugin.api.storage.Storage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

public class ExtractActionJobTest {
	
    private final String BACKUP_JOB_old = loadJson("backup_job_old.json");
    private final String BACKUP_JOB = loadJson("backup_job.json");
    //TODO AL switch to BackupJobDTO within this class
    private final String BACKUP_JOB_DTO = loadJson("backup_job_dto.json");

    // private static final String BACKUP_JOB_FAIL =
    // loadJson("backup_job_fail.json");

    private String loadJson(String fileName) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(fileName));
    }

    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            System.out.println("PROGRESS: " + message);
        }
    };

    public ExtractActionJobTest() throws IOException {
    }

    @Before
    public void setup() throws ActionException {
        System.out.println("Setting up test index...");
        // Dummy storage reader on the src/test/resources directory
        Storage storage = new DummyStorage();


        BackupJobExecutionDTO job = JsonSerializer.deserialize(this.BACKUP_JOB_DTO, BackupJobExecutionDTO.class);

        // Extract test files on the local ES index
        ExtractorAction action = new ExtractorAction();

        // now call the actual extraction (Metadata extraction, Tika analysis)
        action.doAction(new HashMap<String, String>(), new HashMap<String, String>(), new ArrayList<String>(), storage, job, this.logProgressable);
        System.out.println("Done.");
    }

    @After
    public void tearDown() {
        // the directory is backmeup-plugins/backmeup-indexing-plugin/data
        try {
			FileUtils.deleteDirectory(new File("data"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void test() {
    	
    }
}
