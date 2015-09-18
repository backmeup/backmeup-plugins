package org.backmeup.facebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.backmeup.facebook.htmlgenerator.HTMLGenerator;
import org.backmeup.facebook.metadata.MetaInfoExtractor;
import org.backmeup.facebook.storage.Serializer;
import org.backmeup.facebook.utils.FileUtils;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.Datasource;
import org.backmeup.plugin.api.DatasourceException;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

public class FacebookDatasource implements Datasource {

    @Override
    public void downloadAll(PluginProfileDTO pluginProfile, PluginContext pluginContext, Storage storage, Progressable progressor) 
            throws DatasourceException, StorageException {
        String currentAccessToken = pluginProfile.getAuthData().getProperties()
                .get(FacebookHelper.RT_PROPERTY_ACCESS_TOKEN);

        String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "facebook_"
                + System.currentTimeMillis();
        File dataDir = new File(tempDir, FacebookHelper.getProperty(FacebookHelper.PROPERTY_DATA_DIR));
        File htmlDir = new File(tempDir, FacebookHelper.getProperty(FacebookHelper.PROPERTY_HTML_DIR));

        try {
            FacebookClient fbc = new DefaultFacebookClient(currentAccessToken, Version.VERSION_2_3);
            Serializer.generateAll(fbc, dataDir, FacebookHelper.getDebugSkipAlbums(), FacebookHelper.getDebugMaxPics(),
                    progressor);

            HTMLGenerator mainGen = new HTMLGenerator(htmlDir, dataDir);
            mainGen.genOverview();

            // get all xml files
            for (File file : FileUtils.files(dataDir)) {
                registerFile(dataDir.getParentFile(), file, storage);
            }
            // get all html files
            for (File file : FileUtils.files(htmlDir)) {
                registerFile(htmlDir, file, storage);
            }

            storage.addFile(new FileInputStream(dataDir), "xmldata", new MetainfoContainer());
            storage.addFile(new FileInputStream(htmlDir), "html", new MetainfoContainer());
        } catch (IOException e) {
            throw new DatasourceException(e);
        }
    }

    public static void registerFile(File root, File file, Storage storage) throws IOException, StorageException {
        // iterate through the html and xml files and extract standardized
        // metadata (geo location, dates, etc.)
        MetainfoContainer metaInfoContainer = new MetainfoContainer();
        Metainfo metaInfo = new MetaInfoExtractor().extract(file);
        metaInfoContainer.addMetainfo(metaInfo);

        // add this file to the storage
        String path = FileUtils.getWayTo(root.getParentFile(), file);
        storage.addFile(new FileInputStream(file), path.substring(2, path.length() - 1), metaInfoContainer);
    }
}
