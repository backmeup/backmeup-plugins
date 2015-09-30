package org.backmeup.filegenerator;

import java.io.File;
import java.io.IOException;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FilegeneratorDatasourceTest {
    private Storage storage;
    private PluginContext pluginContext;
    private File tempStorage;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setup() throws StorageException, IOException {
        tempStorage = temp.newFolder();

        storage = new LocalFilesystemStorage();
        storage.open(tempStorage.getPath());

        pluginContext = new PluginContext();
        pluginContext.setAttribute("org.backmeup.tmpdir", "test");
    }

    @After
    public void tearDown() throws StorageException {
        // Junit automatically cleans up temporary storage directories
    }

    @Test
    public void testGenerateAllFileTypes() throws StorageException {
        PluginProfileDTO fileGenProfile = getProfileFilegenerator();
        FilegeneratorDatasource datasource = new FilegeneratorDatasource();
        
        // Generate 10 files + 10 metadata when adding them to storage)
        datasource.downloadAll(fileGenProfile, pluginContext, storage, logProgressable);
        
        Assert.assertEquals(20, tempStorage.listFiles().length);
    }

    public static PluginProfileDTO getProfileFilegenerator() {
        String pluginId = "org.backmeup.filegenerator";
        PluginType profileType = PluginType.Source;

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);

        pluginProfile.addProperty("text", "true");
        pluginProfile.addProperty("image", "true");
        pluginProfile.addProperty("pdf", "true");
        pluginProfile.addProperty("binary", "true");

        return pluginProfile;
    }

    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            System.out.println("PROGRESS: " + message);
        }
    };
}
