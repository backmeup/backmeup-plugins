package org.backmeup.twitter;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

/**
 * The TwitterDescriptor provides all necessary information about this plugin.
 * Note: TWITTER_ID matches the filters stated in the configuration files:
 * META-INF/spring/org.backmeup.twitter-context.xml
 * META-INF/spring/org.backmeup.twitter-osgi-context.xml
 */
public class TwitterDescriptor extends BaseSourceSinkDescribable {
	public static final String TWITTER_ID = "org.backmeup.twitter";

	@Override
	public String getId() {
		return TWITTER_ID;
	}

	@Override
	public String getTitle() {
		return "BackMeUp Twitter Plug-In";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of downloading from twitter";
	}

	@Override
	public String getImageURL() {
		return "https://twitter.com/images/resources/twitter-bird-light-bgs.png";
	}

	@Override
	public PluginType getType() {
		return PluginType.Source;
	}

	@Override
	public Properties getMetadata(Properties accessData) {
		Properties metadata = new Properties();
		metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
		return metadata;
	}

}
