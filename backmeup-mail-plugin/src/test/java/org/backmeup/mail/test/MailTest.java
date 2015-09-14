package org.backmeup.mail.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.backmeup.mail.MailDatasource;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;

public class MailTest {
  public static void main(String[] args) throws Exception {
 // Use the properties saved during DropboxAuthenticate to download all files from Dropbox
      Map<String, String> authProps = new HashMap<>();
//    authProps.load(MailTest.class.getClassLoader().getResourceAsStream("auth.props"));    
    
    Map<String, String> props = new HashMap<>();
    List<String> options = new ArrayList<>();
    
    MailDatasource source = new MailDatasource();
    Storage storage = new LocalFilesystemStorage();
    storage.open("C:/TEMP/TEST/");
    source.downloadAll(new PluginProfileDTO(), new PluginContext(), storage, new Progressable() {
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
