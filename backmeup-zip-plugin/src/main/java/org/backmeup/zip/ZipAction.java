package org.backmeup.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.Action;
import org.backmeup.plugin.api.ActionException;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.zip.constants.Constants;
import org.backmeup.zip.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipAction implements Action {
    private static final String ZIP_FILE_EXTENSION = ".zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipAction.class);

    @Override
    public void doAction(PluginProfileDTO profile, PluginContext context, Storage storage, Progressable progressor)
            throws ActionException, StorageException {

        
        String tempOutDir = getTempDirectoryPath(context);
        String tempOutFileName = UUID.randomUUID() + ZIP_FILE_EXTENSION + ".tmp";
        String tempOutFilePath = tempOutDir + "/" + tempOutFileName;
        
        File temp = new File(tempOutDir);
        if (!temp.exists() && !temp.mkdirs()) {
            throw new PluginException(Constants.BACKMEUP_ZIP_ID, "Unable to create directory " + temp);
        }

        String tmpDirName = context.getAttribute("org.backmeup.tmpdir", String.class);

        if (tmpDirName == null) {
            throw new PluginException(Constants.BACKMEUP_ZIP_ID,
                    "Error: org.backmeup.tmpdir property has not been set!");
        }

        String zipFileName = tmpDirName + "_" + new Date().getTime() + ZIP_FILE_EXTENSION;
        String zipFilePath = "/" + zipFileName;
        LOGGER.info("Creating zip backup file: " + zipFileName);

        // Create and write temporary zip file
        File zipFile = createZipFile(storage, tempOutFilePath);

        // Put newly created zip file in storage
        LOGGER.info("Add zip file to local storage.");
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            MetainfoContainer metaContainer = new MetainfoContainer();
            Metainfo meta = new Metainfo();
            meta.setBackupDate(new Date());
            meta.setCreated(new Date());
            metaContainer.addMetainfo(meta);

            storage.addFile(fis, zipFilePath, metaContainer);
        } catch (FileNotFoundException e) {
            throw new PluginException(Constants.BACKMEUP_ZIP_ID, "Could not find created zip file", e);
        } catch (IOException e) {
            throw new PluginException(Constants.BACKMEUP_ZIP_ID, "Could not read created zip file", e);
        }
        
        // Delete temporary zip file
        if(!zipFile.delete()){
            throw new PluginException(Constants.BACKMEUP_ZIP_ID, "Could not delete temp zip file");
        }
    }

    private File createZipFile(Storage storage, String tempFilePath) throws StorageException {
        try (FileOutputStream fos = new FileOutputStream(tempFilePath);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ZipOutputStream zos = new ZipOutputStream(bos);) {

            zos.setEncoding("UTF-8");
            Iterator<DataObject> it = storage.getDataObjects();
            while (it.hasNext()) {
                DataObject entry = it.next();

                // Normalize path string
                String entryPath = entry.getPath();
                if (entryPath.startsWith("/") || entryPath.startsWith("\\")) {
                    entryPath = entryPath.substring(1);
                }

                // Put data object in zip container
                LOGGER.info("Putting entry to zip: " + entryPath);
                zos.putNextEntry(new ZipEntry(entryPath));
                zos.write(entry.getBytes());
                zos.closeEntry();

                // Remove data object from storage
                storage.removeFile(entry.getPath());
            }

            LOGGER.info("Zip file created.");
            return new File(tempFilePath);
        } catch (IOException ex) {
            throw new PluginException(Constants.BACKMEUP_ZIP_ID, "An exception occurred during zip creation!", ex);
        }
    }
    
    private String getTempDirectoryPath(PluginContext context) {
        String path = context.getAttribute("org.backmeup.zip.tempDir", String.class);
        if (path == null) {
            path = PropertiesUtil.getInstance().getProperty(Constants.PROPERTY_TEMP_DIR);
        }
        if(path == null) {
            throw new PluginException(Constants.BACKMEUP_ZIP_ID,
                    "Cannot find temporary output directory");
        }
        
        return path;
    }
}
