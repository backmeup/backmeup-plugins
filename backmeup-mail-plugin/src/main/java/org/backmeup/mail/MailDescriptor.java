package org.backmeup.mail;

import java.util.HashMap;
import java.util.Map;

import org.backmeup.plugin.api.BaseSourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

/**
 * The MailDescriptor provides all necessary information about this plugin.
 * Note: MAIL_ID matches the filters stated in the configuration files:
 * META-INF/spring/org.backmeup.mail-context.xml
 * META-INF/spring/org.backmeup.mail-osgi-context.xml
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
    public Map<String, String> getMetadata(Map<String, String> accessData) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Metadata.BACKUP_FREQUENCY, "daily");
        metadata.put(Metadata.DYNAMIC_OPTIONS, "true");
        return metadata;
    }

}
