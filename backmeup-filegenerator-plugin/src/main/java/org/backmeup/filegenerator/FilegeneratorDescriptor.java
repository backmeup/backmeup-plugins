package org.backmeup.filegenerator;

import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

public class FilegeneratorDescriptor implements SourceSinkDescribable {
  private static final String FILEGENERATOR_ID = "org.backmeup.filegenerator";
  
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
    return "a plugin for testing purposes that generates files";
  }

  @Override
  public Properties getMetadata(Properties accessData) {
    Properties props = new Properties();
    props.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
    props.setProperty("FILE_TYPE", "txt,pdf,jpg");
    props.setProperty("FILE_SIZE", "50k");
    props.setProperty("FILE_COUNT", "3");
    props.setProperty("DURATION", "5s");
    return props;
  }

  @Override
  public Type getType() {
    return Type.Source;
  }

  @Override
  public String getImageURL() {
    return "https://backmeup.at/dummy.png";
  }

}