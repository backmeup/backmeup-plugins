package org.backmeup.sftp;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.BaseSourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

/**
 * The DropboxDescriptor provides all necessary information about this plugin.
 * Note: DROPBOX_ID matches the filters stated in the configuration files:
 * META-INF/spring/org.backmeup.dropbox-context.xml
 * META-INF/spring/org.backmeup.dropbox-osgi-context.xml
 * 
 * @author fschoeppl
 */
public class SftpDescriptor extends BaseSourceSinkDescribable {
	public static final String SFTP_ID = "org.backmeup.sftp";

	@Override
	public String getId() {
		return SFTP_ID;
	}

	@Override
	public String getTitle() {
		return "BackMeUp sftp Plug-In";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of downloading data from sftp";
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
	public Map<String, String> getMetadata(Map<String, String> accessData) {
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(Metadata.BACKUP_FREQUENCY, "daily");
		metadata.put(Metadata.DYNAMIC_OPTIONS, "true");
		return metadata;
	}

}
