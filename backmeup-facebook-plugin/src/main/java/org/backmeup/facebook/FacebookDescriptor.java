package org.backmeup.facebook;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

public class FacebookDescriptor extends BaseSourceSinkDescribable {
    public static final String ID = "org.backmeup.facebook";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getTitle() {
        return "Backmeup Facebook Plugin R2";
    }

    @Override
    public String getDescription() {
        return "A plugin which provides the Facebook datasource";
    }

    @Override
    public PluginType getType() {
        return PluginType.Source;
    }

    @Override
    public String getImageURL() {
        return "https://backmeup.at/facebook.png";
    }

    @Override
    public Map<String, String> getMetadata(Map<String, String> authData) {
        HashMap<String, String> props = new HashMap<>();
        props.put(Metadata.BACKUP_FREQUENCY, "daily");
        return props;
    }

}
