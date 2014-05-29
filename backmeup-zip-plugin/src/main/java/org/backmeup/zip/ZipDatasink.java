package org.backmeup.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;

public class ZipDatasink implements Datasink {
  private final Logger logger = Logger.getLogger(ZipDatasink.class.getName());
  
  @Override
  public String upload(Properties accessData, Storage storage,
      Progressable progressor) {
	  
    ZipHelper zipHelper = ZipHelper.getInstance();
    String tmpDir = accessData.getProperty ("org.backmeup.tmpdir");
    String userId = accessData.getProperty ("org.backmeup.userid");
    
    if (tmpDir == null) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Error: org.backmeup.tmpDir property has not been set!");
    }
    
    if (userId == null) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Error: org.backmeup.userid property has not been set!");
    }
    
    String fileName = tmpDir + "_" + new Date().getTime() +".zip";
    logger.log(Level.FINE, "Creating zip backup file: " + fileName);
    String path = MessageFormat.format(zipHelper.getTemporaryPath(), userId) + fileName;
    logger.log(Level.FINE, "Path zip backup path: " + path);
    
    new File(path).getParentFile().mkdirs();
    try (FileOutputStream fos = new FileOutputStream(path); ZipOutputStream zos = new ZipOutputStream(fos)) {
      // create folder to file
      // create zip file
      zos.setEncoding ("UTF-8");
      Iterator<DataObject> it = storage.getDataObjects();
      while(it.hasNext()) {
        DataObject entry = it.next();
        String entryPath = entry.getPath();
        if (entryPath.startsWith("/") || entryPath.startsWith("\\"))
          entryPath = entryPath.substring(1);
        logger.log(Level.FINE, "Putting entry to zip: " + entryPath);
        zos.putNextEntry(new ZipEntry(entryPath));
        zos.write(entry.getBytes());
        zos.closeEntry();
      }
      logger.log(Level.FINE, "Zip file created.");
      if (zipHelper.isRemote()) {
        logger.log(Level.FINE, "Sending zip file to sftp destination...");
        try (InputStream stream = new FileInputStream(path)) {
          zipHelper.sendToSftpDestination(stream, fileName, userId);
        }
      }
    } catch (Exception ex) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "An exception occurred during zip creation!", ex);
    }
    return null;
  }

}
