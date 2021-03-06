package org.backmeup.plugin.storage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.Datasink;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.storage.constants.Constants;
import org.backmeup.storage.api.StorageClient;
import org.backmeup.storage.client.BackmeupStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BackmeupStorageDatasink class uploads all elements from the StorageReader up to the Backmeup-Storage service
 * based on the connection string of a certain user.
 * 
 */
public class BackmeupStorageDatasink implements Datasink {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackmeupStorageDatasink.class);
    
    private static final String FIELD_THUMBNAIL_PATH = "thumbnail_path";

    @Override
    public String upload(PluginProfileDTO pluginProfile, PluginContext pluginContext, Storage storage,
            Progressable progressor) throws StorageException {
        progressor.progress("Start backmeup-storage-plugin");

        String tmpDir = pluginContext.getAttribute("org.backmeup.tmpdir", String.class);
        if (tmpDir == null) {
            tmpDir = "";
        }

        progressor.progress("Get connection string");
        progressor.progress("AuthData:");
        Map<String, String> authData = pluginProfile.getAuthData().getProperties();
        for (Entry<String,String> entry : authData.entrySet()) {
            progressor.progress(entry.getKey() + ": " + entry.getValue());
        }

        String storageUrl = authData.get(Constants.PROP_STORAGE_URL);
        progressor.progress("Storage url= " + storageUrl);

        progressor.progress("Initialized storage client");
        StorageClient client = new BackmeupStorageClient(storageUrl);

        String accessToken;
        try {
            progressor.progress("Authenticate user");
            String username = authData.get(Constants.PROP_USERNAME);
            String password = authData.get(Constants.PROP_PASSWORD);
            accessToken = client.authenticate(username, password);
            progressor.progress("Using Token=" + accessToken);
        } catch (IOException e) {
            throw new StorageException(e);
        }

        Iterator<DataObject> it = storage.getDataObjects();
        int i = 1;
        progressor.progress("Start uploading ...");
        while (it.hasNext()) {
            DataObject dataObj = it.next();

            String filepath = "/" + tmpDir + dataObj.getPath();
            progressor.progress("Filepath: " + filepath);

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(dataObj.getBytes());
                String log = String.format("Uploading file %s (Number: %d)...", filepath, i++);
                progressor.progress(log);
                client.saveFile(accessToken, filepath, true, bis.available(), bis);
                bis.close();

                //check if we've got a thumbnail for this data object
                String thumbnailLocalLocation = extractThumbnailFileLocation(dataObj.getMetainfo());
                if (thumbnailLocalLocation != null) {
                    thumbnailLocalLocation = normalizeThumbnailPath(thumbnailLocalLocation);
                    String destPath = buildThumnailStorageDestinationPath(filepath, thumbnailLocalLocation);
                    uploadThumbnail(client, accessToken, thumbnailLocalLocation, destPath, progressor);
                }
            } catch (IOException e) {
                throw new PluginException(BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID,
                        "Error during upload of file %s", e);
            }
        }
        return null;
    }

    /**
     * @param parentObjectPath
     *            /BMU_filegenerator_553_28_01_2015_00_30/file10.jpg
     * @param thumbnailLocalLocation
     *            is a file path on the local disk e.g.
     *            C:\\data\\thumbnails\\BMU_filegenerator_553_28_01_2015_00_30\\1422401430361_file10.jpg_thumb.jpg
     * @return relative path of thumbnail on storage to ingest into elasticsearch
     */
    private String buildThumnailStorageDestinationPath(String parentOjbectPath, String thumbnailLocalLocation) {
        String fileName = getFilename(thumbnailLocalLocation);
        String pathPrefix = "";
        if (thumbnailLocalLocation.indexOf('/') > -1) {
            pathPrefix = parentOjbectPath.substring(0, parentOjbectPath.lastIndexOf('/'));
        }
        return pathPrefix + "/thumbs/" + fileName;
    }
    
    private String normalizeThumbnailPath(String thumbnailLocalLocation) {
        String normalizedPath = thumbnailLocalLocation;
        if (normalizedPath.indexOf('\\') > -1) {
            normalizedPath = normalizedPath.replace('\\', '/');
        }
        return normalizedPath;
    }

    private String getFilename(String path) {
        if (path.indexOf('/') > -1) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

    private String extractThumbnailFileLocation(MetainfoContainer metaInfo) {
        Iterator<Metainfo> itMeta = metaInfo.iterator();
        String thumnailPath = null;
        while (itMeta.hasNext()) {
            Metainfo mInfo = itMeta.next();
            thumnailPath = mInfo.getAttribute(FIELD_THUMBNAIL_PATH);
        }
        return thumnailPath;
    }

    private void uploadThumbnail(StorageClient client, String accessToken, String thumbLocalStorageLocation,
            String thumbStorageDestinationPath, Progressable progressor) {

        if (thumbStorageDestinationPath != null) {
            progressor.progress("Uploading thumbnail to: " + thumbStorageDestinationPath);
            try (InputStream is = new FileInputStream(thumbLocalStorageLocation)) {
                client.saveFile(accessToken, thumbStorageDestinationPath, true, is.available(), is);
            } catch (PluginException | IOException e) {
                LOGGER.error("", e);
                progressor.progress("Error handing over thumbnail to storage");
            }
        }
    }
}
