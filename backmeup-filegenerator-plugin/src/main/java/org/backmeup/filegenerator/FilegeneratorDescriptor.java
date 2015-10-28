package org.backmeup.filegenerator;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.BaseSourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

public class FilegeneratorDescriptor extends BaseSourceSinkDescribable {
  public static final String FILEGENERATOR_ID = "org.backmeup.filegenerator";
  
  @Override
  public String getId() {
    return FILEGENERATOR_ID;
  }

  @Override
  public String getTitle() {
    return "Backmeup File Generator Plugin";
  }

  @Override
  public String getDescription() {
    return "A plugin for testing purposes that generates files";
  }

  @Override
  public Map<String, String> getMetadata(Map<String, String> accessData) {
      Map<String, String> props = new HashMap<>();
    props.put(Metadata.BACKUP_FREQUENCY, "daily");
    return props;
  }

  @Override
  public PluginType getType() {
    return PluginType.Source;
  }

  @Override
  public String getImageURL() {
    return "https://backmeup.at/dummy.png";
  }

}
