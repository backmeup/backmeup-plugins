package org.backmeup.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipAction implements Action {
    private static final String ZIP_FILE_EXTENSION = ".zip";
    private static final String ZIP_ID = "org.backmeup.zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipAction.class);

    @Override
    public void doAction(PluginProfileDTO profile, PluginContext context, Storage storage, Progressable progressor)
            throws ActionException, StorageException {

        String tmpDir = context.getAttribute("org.backmeup.tmpdir", String.class);
        String userId = context.getAttribute("org.backmeup.userid", String.class);

        if (tmpDir == null) {
            throw new PluginException(ZIP_ID, "Error: org.backmeup.tmpDir property has not been set!");
        }

        if (userId == null) {
            throw new PluginException(ZIP_ID, "Error: org.backmeup.userid property has not been set!");
        }

        String zipFile = tmpDir + "_" + new Date().getTime() + ZIP_FILE_EXTENSION;
        LOGGER.info("Creating zip backup file: " + zipFile);

        boolean created = new File(zipFile).getParentFile().mkdirs();
        if (!created) {
            throw new PluginException(ZIP_ID, "Could not create temp directory");
        }

        // Create and write zip file
        try (FileOutputStream fos = new FileOutputStream(zipFile);
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
        } catch (Exception ex) {
            throw new PluginException(ZIP_ID, "An exception occurred during zip creation!", ex);
        }

        // Put newly created zip file in storage
        LOGGER.info("Add zip file to local stroage.");
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            MetainfoContainer metaContainer = new MetainfoContainer();
            Metainfo meta = new Metainfo();
            meta.setBackupDate(new Date());
            meta.setCreated(new Date());
            metaContainer.addMetainfo(meta);

            storage.addFile(fis, "/", metaContainer);
        } catch (FileNotFoundException e) {
            throw new PluginException(ZIP_ID, "Could not find created zip file", e);
        } catch (IOException e) {
            throw new PluginException(ZIP_ID, "Could not read created zip file", e);
        }
    }
}
