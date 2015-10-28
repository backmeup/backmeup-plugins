package org.backmeup.zip;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

public class ZipDescriptor extends BaseSourceSinkDescribable {
    public static final String ZIP_ID = "org.backmeup.zip";

    @Override
    public String getId() {
        return ZIP_ID;
    }

    @Override
    public String getTitle() {
        return "ZIP-Download";
    }

    @Override
    public String getDescription() {
        return "Stores the backup within a zip file";
    }

    @Override
    public Properties getMetadata(Properties accessData) {
        Properties metadata = new Properties();
        metadata.setProperty(Metadata.STORAGE_ALWAYS_ACCESSIBLE, "true");
        return metadata;
    }

    @Override
    public PluginType getType() {
        return PluginType.Sink;
    }

    @Override
    public String getImageURL() {
        return "";
    }

}