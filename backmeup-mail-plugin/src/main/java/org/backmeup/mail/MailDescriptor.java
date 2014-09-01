package org.backmeup.mail;

import java.util.Properties;

import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.connectors.BaseSourceSinkDescribable;

/**
 * The DropboxDescriptor provides all necessary information about this plugin.
 * Note: DROPBOX_ID matches the filters stated in the configuration files:
 * META-INF/spring/org.backmeup.dropbox-context.xml
 * META-INF/spring/org.backmeup.dropbox-osgi-context.xml
 * 
 * @author fschoeppl
 */
public class MailDescriptor extends BaseSourceSinkDescribable {
	public static final String MAIL_ID = "org.backmeup.mail";

	@Override
	public String getId() {
		return MAIL_ID;
	}

	@Override
	public String getTitle() {
		return "BackMeUp Mail Plug-In";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of downloading e-mails";
	}

	@Override
	public String getImageURL() {
		return "http://about:blank";
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
