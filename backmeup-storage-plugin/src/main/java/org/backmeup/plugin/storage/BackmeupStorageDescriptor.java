package org.backmeup.plugin.storage;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

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
    public Properties getMetadata(Properties accessData) {
        Properties metadata = new Properties();
        metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.setProperty(Metadata.STORAGE_ALWAYS_ACCESSIBLE, "true");
        return metadata;
    }

}
