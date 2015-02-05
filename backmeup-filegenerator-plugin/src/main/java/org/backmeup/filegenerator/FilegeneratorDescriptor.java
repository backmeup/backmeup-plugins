package org.backmeup.filegenerator;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

public class FilegeneratorDescriptor extends BaseSourceSinkDescribable {
  static final String FILEGENERATOR_ID = "org.backmeup.filegenerator";
  
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
  public Properties getMetadata(Properties accessData) {
    Properties props = new Properties();
    props.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
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
