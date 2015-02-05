package org.backmeup.dummy;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

public class DummyDescriptor extends BaseSourceSinkDescribable {

    static final String DUMMY_ID = "org.backmeup.dummy";

    @Override
    public String getId() {
        return DUMMY_ID;
    }

    @Override
    public String getTitle() {
        return "dummy";
    }

    @Override
    public String getDescription() {
        return "a plugin for testing purposes";
    }

    @Override
    public Map<String, String> getMetadata(@SuppressWarnings("unused") Map<String, String> accessData) {
        Map<String, String> props = new HashMap<>();
        props.put(Metadata.BACKUP_FREQUENCY, "daily");
        props.put(Metadata.FILE_SIZE_LIMIT, "100");
        props.put(Metadata.QUOTA, "50");
        props.put(Metadata.QUOTA_LIMIT, "200");
        props.put(Metadata.STORAGE_ALWAYS_ACCESSIBLE, "true");
        return props;
    }

    @Override
    public PluginType getType() {
        return PluginType.SourceSink;
    }

    @Override
    public String getImageURL() {
        return "https://backmeup.at/dummy.png";
    }

}
