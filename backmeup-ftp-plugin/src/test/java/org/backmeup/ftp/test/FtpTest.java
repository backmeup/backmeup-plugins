package org.backmeup.ftp.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.backmeup.ftp.FtpDatasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;

public class FtpTest {
  public static void main(String[] args) throws Exception {
 // Use the properties saved during DropboxAuthenticate to download all files from Dropbox
    Properties authProps = new Properties();
    authProps.load(FtpTest.class.getClassLoader().getResourceAsStream("auth.props"));    
    
    Properties props = new Properties();
    List<String> options = new ArrayList<>();
    
    FtpDatasource source = new FtpDatasource();
    Storage storage = new LocalFilesystemStorage();
    storage.open("C:/TEMP/TEST/");
    source.downloadAll(authProps, props, options, storage, new Progressable() {
      @Override
      public void progress(String message) {}
    });
    
    // StorageReader sr = new LocalFilesystemStorageReader();
    // sr.open("C:/TEMP/TEST/");
    Iterator<DataObject> it = storage.getDataObjects();
    while (it.hasNext()) {
      DataObject da = it.next();
      System.out.println(da.getMetainfo());
      System.out.println();
    }
  }
}
