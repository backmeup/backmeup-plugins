package org.backmeup.plugin.storage.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.storage.BackmeupStorageDescriptor;

/**
 * This Util class constructs loads properties form the file storage.properties
 * found within this bundles jar file.
 * 
 */
public class PropertiesUtil {
    public static final String PROPERTIES_FILE = "storage.properties";
    public static final String PROPERTY_URL = "storage.url";

    private static PropertiesUtil propertiesUtil;
    private static Properties properties;

    private PropertiesUtil() {

    }

    public static PropertiesUtil getInstance() {
        if (propertiesUtil == null) {
            propertiesUtil = new PropertiesUtil();
        }

        if (properties == null) {
            propertiesUtil.loadProperties();
        }
        return propertiesUtil;
    }

    private void loadProperties() {
        Properties properties = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                PROPERTIES_FILE);
        if (is == null)
            throw new PluginException(
                    BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID,
                    "Fatal error: cannot find dropbox.properties within jar-file!");

        try {
            properties.load(is);
            is.close();
        } catch (IOException e) {
            throw new PluginException(
                    BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID,
                    "Fatal error: could not load dropbox.properties: "
                            + e.getMessage(), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
        }

    }

    public String getProperty(final String key) {
        if (key != null && !key.isEmpty()) {
            return properties.getProperty(key);
        }
        return null;
    }
}
