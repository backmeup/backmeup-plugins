package org.backmeup.plugin.storage;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.BaseSourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.storage.constants.Constants;
import org.backmeup.plugin.storage.utils.PropertiesUtil;

/**
 * The BackmeupStorageDescriptor provides all necessary information about this plugin.
 * 
 */
public class BackmeupStorageDescriptor extends BaseSourceSinkDescribable {
    public static final String BACKMEUP_STORAGE_ID = "org.backmeup.storage";

    @Override
    public String getId() {
        return BACKMEUP_STORAGE_ID;
    }

    @Override
    public String getTitle() {
        return "Backmeup-Storage Plug-In";
    }

    @Override
    public String getDescription() {
        return "A plug-in that is capable of uploading data to a Backmeup-Storage service";
    }

    @Override
    public String getImageURL() {
        return "http://about:blank";
    }

    @Override
    public PluginType getType() {
        return PluginType.Sink;
    }

    @Override
    public Map<String, String> getMetadata(Map<String, String> accessData) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.put(Metadata.STORAGE_ALWAYS_ACCESSIBLE, "true");
        
        String downloadBase = PropertiesUtil.getInstance().getProperty(Constants.PROP_DOWNLOAD_BASE);
        metadata.put(Metadata.DOWNLOAD_BASE, downloadBase);
        
        return metadata;
    }

}
