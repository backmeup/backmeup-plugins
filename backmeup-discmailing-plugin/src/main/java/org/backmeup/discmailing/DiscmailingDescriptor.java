package org.backmeup.discmailing;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

public class DiscmailingDescriptor extends BaseSourceSinkDescribable {
    public static final String DISC_ID = "org.backmeup.discmailing";

    @Override
    public String getId() {
        return DISC_ID;
    }

    @Override
    public String getTitle() {
        return "Discmailing";
    }

    @Override
    public String getDescription() {
        return "A plug-in that is capable of uploading data to DVD burning station";
    }

    @Override
    public Properties getMetadata(Properties accessData) {
        Properties metadata = new Properties();
        metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.setProperty(Metadata.STORAGE_ALWAYS_ACCESSIBLE, "false");
        return metadata;
    }

    @Override
    public PluginType getType() {
        return PluginType.Sink;
    }

    @Override
    public String getImageURL() {
        return "https://backmeup.at/dummy.png";
    }
}
