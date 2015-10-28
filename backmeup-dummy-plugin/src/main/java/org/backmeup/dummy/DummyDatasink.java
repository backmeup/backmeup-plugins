package org.backmeup.dummy;

import java.util.Iterator;
import java.util.Map.Entry;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.Datasink;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class DummyDatasink implements Datasink {

    @Override
    public String upload(PluginProfileDTO pluginProfile, PluginContext context, Storage storage, Progressable progressor) throws StorageException {

        progressor.progress("Uploading to StorageReader");

        Iterator<DataObject> it = storage.getDataObjects();
        while (it.hasNext()) {
            DataObject obj = it.next();
            Iterator<Metainfo> infos = obj.getMetainfo().iterator();
            if (infos.hasNext()) {
                progressor.progress("=============================================");
                progressor.progress("Metainfos of object:\t\t" + obj.getPath());
                while (infos.hasNext()) {
                    Metainfo info = infos.next();
                    for (Entry<Object, Object> entry : info.getAttributes().entrySet()) {
                        progressor.progress(entry.getKey() + ":\t\t" + entry.getValue());
                    }
                }
            }
        }
        
        return "not used";
    }

}
