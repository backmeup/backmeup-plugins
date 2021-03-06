package org.backmeup.dropbox;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.BaseSourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.WebAuthSession;

/**
 * The DropboxDescriptor provides all necessary information about this plugin. Note: DROPBOX_ID matches the filters
 * stated in the configuration files: META-INF/spring/org.backmeup.dropbox-context.xml
 * META-INF/spring/org.backmeup.dropbox-osgi-context.xml
 * 
 * @author fschoeppl
 */
public class DropboxDescriptor extends BaseSourceSinkDescribable {
    public static final String DROPBOX_ID = "org.backmeup.dropbox";

    @Override
    public String getId() {
        return DROPBOX_ID;
    }

    @Override
    public String getTitle() {
        return "BackMeUp Dropbox Plug-In";
    }

    @Override
    public String getDescription() {
        return "A plug-in that is capable of downloading and uploading from dropbox";
    }

    @Override
    public String getImageURL() {
        return "http://about:blank";
    }

    @Override
    public PluginType getType() {
        return PluginType.SourceSink;
    }

    @Override
    public Map<String, String> getMetadata(Map<String, String> accessData) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.put(Metadata.FILE_SIZE_LIMIT, "150");
        // Note: The api cannot retrieve the total free space of dropbox.
        // The free version of dropbox currently has a capacity of 2GB.
        metadata.put(Metadata.QUOTA_LIMIT, "2048");

        try {
            if (accessData != null && !accessData.isEmpty()) {
                DropboxAPI<WebAuthSession> api = DropboxHelper.getInstance().getApi(accessData);
                if (api.getSession().isLinked()) {
                    double quota_limit = (double) api.accountInfo().quota / (1024.f * 1024.f);
                    double quota = (double) api.accountInfo().quotaNormal / (1024.f * 1024.f);
                    metadata.put(Metadata.QUOTA_LIMIT, Double.toString(quota_limit));
                    metadata.put(Metadata.QUOTA, Double.toString(quota));
                }
            }
        } catch (Exception ex) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID, "Could not load account metadata", ex);
        }

        metadata.put(Metadata.STORAGE_ALWAYS_ACCESSIBLE, "true");
        metadata.put(Metadata.DYNAMIC_OPTIONS, "true");
        return metadata;
    }

}
